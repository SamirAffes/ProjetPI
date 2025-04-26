package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import entities.ENUMS.EtatReclamation;
import entities.Reclamation;
import lombok.extern.slf4j.Slf4j;
import utils.db_context;

@Slf4j
public class ReclamationService implements CRUD<Reclamation> {
    
    private final Connection conn = db_context.getInstance().getConn();

    @Override
    public void ajouter(Reclamation t) {
        String sql = "INSERT INTO reclamation (type, description, date, etat, user_id, chauffeur_id, organisme_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, t.getType());
            stmt.setString(2, t.getDescription());
            stmt.setTimestamp(3, new Timestamp(t.getDate().getTime()));
            stmt.setString(4, t.getEtat().toString());
            stmt.setInt(5, t.getUser_id());
            stmt.setInt(6, t.getChauffeur_id());
            stmt.setInt(7, t.getOrganisme_id());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                log.info("Reclamation ajoutée avec succès");
            } else {
                log.error("Échec de l'ajout de la réclamation");
            }
        } catch (SQLException e) {
            log.error("Erreur lors de l'ajout de la réclamation: {}", e.getMessage());
            throw new RuntimeException("Erreur d'ajout de la réclamation", e);
        }
    }

    @Override
    public void supprimer(Reclamation t) {
        String sql = "DELETE FROM reclamation WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, t.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                log.info("Reclamation supprimée avec succès");
            } else {
                log.error("Aucune réclamation trouvée avec l'ID {}", t.getId());
            }
        } catch (SQLException e) {
            log.error("Erreur lors de la suppression de la réclamation: {}", e.getMessage());
            throw new RuntimeException("Erreur de suppression de la réclamation", e);
        }
    }

    @Override
    public void modifier(Reclamation t) {
        String sql = "UPDATE reclamation SET type = ?, description = ?, date = ?, etat = ?, user_id = ?, chauffeur_id = ?, organisme_id = ? WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, t.getType());
            stmt.setString(2, t.getDescription());
            stmt.setTimestamp(3, new Timestamp(t.getDate().getTime()));
            stmt.setString(4, t.getEtat().toString());
            stmt.setInt(5, t.getUser_id());
            stmt.setInt(6, t.getChauffeur_id());
            stmt.setInt(7, t.getOrganisme_id());
            stmt.setInt(8, t.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                log.info("Reclamation modifiée avec succès");
            } else {
                log.error("Aucune réclamation trouvée avec l'ID {}", t.getId());
            }
        } catch (SQLException e) {
            log.error("Erreur lors de la modification de la réclamation: {}", e.getMessage());
            throw new RuntimeException("Erreur de modification de la réclamation", e);
        }
    }

    @Override
    public Reclamation afficher(int id) {
        String sql = "SELECT * FROM reclamation WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapReclamationFromResultSet(rs);
                } else {
                    log.warn("Aucune réclamation trouvée avec l'ID {}", id);
                    return null;
                }
            }
        } catch (SQLException e) {
            log.error("Erreur lors de la récupération de la réclamation: {}", e.getMessage());
            throw new RuntimeException("Erreur de récupération de la réclamation", e);
        }
    }

    @Override
    public List<Reclamation> afficher_tout() {
        List<Reclamation> reclamations = new ArrayList<>();
        String sql = "SELECT * FROM reclamation";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                reclamations.add(mapReclamationFromResultSet(rs));
            }
            
            log.info("{} réclamations récupérées", reclamations.size());
            return reclamations;
        } catch (SQLException e) {
            log.error("Erreur lors de la récupération des réclamations: {}", e.getMessage());
            throw new RuntimeException("Erreur de récupération des réclamations", e);
        }
    }
    
    // Méthode qui prend une ligne de la base (ResultSet) et la transforme en objet Reclamation que tu peux manipuler facilement .
    private Reclamation mapReclamationFromResultSet(ResultSet rs) throws SQLException {
        Reclamation reclamation = new Reclamation();
        reclamation.setId(rs.getInt("id"));
        reclamation.setType(rs.getString("type"));
        reclamation.setDescription(rs.getString("description"));
        reclamation.setDate(rs.getTimestamp("date"));
        reclamation.setEtat(EtatReclamation.valueOf(rs.getString("etat")));
        reclamation.setUser_id(rs.getInt("user_id"));
        reclamation.setChauffeur_id(rs.getInt("chauffeur_id"));
        reclamation.setOrganisme_id(rs.getInt("organisme_id"));
        return reclamation;
    }
    
    // Des méthodes supplémentaires pour des fonctionnalités spécifiques
    
    public List<Reclamation> rechercherParType(String type) {
        List<Reclamation> reclamations = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE type LIKE ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + type + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reclamations.add(mapReclamationFromResultSet(rs));
                }
            }
            
            log.info("{} réclamations trouvées pour le type '{}'", reclamations.size(), type);
            return reclamations;
        } catch (SQLException e) {
            log.error("Erreur lors de la recherche de réclamations par type: {}", e.getMessage());
            throw new RuntimeException("Erreur de recherche de réclamations", e);
        }
    }
    
    public List<Reclamation> filtrerParEtat(EtatReclamation etat) {
        List<Reclamation> reclamations = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE etat = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, etat.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reclamations.add(mapReclamationFromResultSet(rs));
                }
            }
            
            log.info("{} réclamations trouvées avec l'état '{}'", reclamations.size(), etat);
            return reclamations;
        } catch (SQLException e) {
            log.error("Erreur lors du filtrage des réclamations par état: {}", e.getMessage());
            throw new RuntimeException("Erreur de filtrage des réclamations", e);
        }
    }
}
