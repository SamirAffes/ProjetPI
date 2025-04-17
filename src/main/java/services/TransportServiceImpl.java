package services;

import entities.Transport;
import utils.DbContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransportServiceImpl implements TransportService {
    private static final Logger LOGGER = Logger.getLogger(TransportServiceImpl.class.getName());
    private final Connection con = DbContext.getInstance().getConn();
    
    @Override
    public void ajouter(Transport t) {
        String query = "INSERT INTO transports (name, type_id, capacity, company_id, license_plate, is_available) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getName());
            ps.setInt(2, t.getTypeId());
            ps.setInt(3, t.getCapacity());
            ps.setInt(4, t.getCompanyId());
            ps.setString(5, t.getLicensePlate());
            ps.setBoolean(6, t.isAvailable());
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        t.setId(rs.getInt(1));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding transport", e);
        }
    }

    @Override
    public void supprimer(Transport t) {
        supprimer(t.getId());
    }
    
    public boolean supprimer(int id) {
        String query = "DELETE FROM transports WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting transport", e);
            return false;
        }
    }

    @Override
    public void modifier(Transport t) {
        String query = "UPDATE transports SET name = ?, type_id = ?, capacity = ?, company_id = ?, license_plate = ?, is_available = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, t.getName());
            ps.setInt(2, t.getTypeId());
            ps.setInt(3, t.getCapacity());
            ps.setInt(4, t.getCompanyId());
            ps.setString(5, t.getLicensePlate());
            ps.setBoolean(6, t.isAvailable());
            ps.setInt(7, t.getId());
            
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating transport", e);
        }
    }
    
    @Override
    public Transport afficher(int id) {
        return getById(id);
    }
    
    @Override
    public List<Transport> afficher_tout() {
        return getAll();
    }

    public List<Transport> getAll() {
        List<Transport> transports = new ArrayList<>();
        String query = "SELECT t.*, tt.name as type_name FROM transports t JOIN transport_types tt ON t.type_id = tt.id";
        
        try (PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Transport transport = mapResultSetToTransport(rs);
                transports.add(transport);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching all transports", e);
        }
        
        return transports;
    }

    public Transport getById(int id) {
        String query = "SELECT t.*, tt.name as type_name FROM transports t JOIN transport_types tt ON t.type_id = tt.id WHERE t.id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransport(rs);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching transport by id", e);
        }
        
        return null;
    }
    
    @Override
    public List<Transport> getTransportsByCompanyId(int companyId) {
        List<Transport> transports = new ArrayList<>();
        String query = "SELECT t.*, tt.name as type_name FROM transports t JOIN transport_types tt ON t.type_id = tt.id WHERE t.company_id = ?";
        
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, companyId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transport transport = mapResultSetToTransport(rs);
                    transports.add(transport);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching transports by company id", e);
        }
        
        return transports;
    }
    
    @Override
    public List<Transport> getTransportsByRouteId(int routeId) {
        List<Transport> transports = new ArrayList<>();
        String query = "SELECT t.*, tt.name as type_name FROM transports t " +
                      "JOIN transport_types tt ON t.type_id = tt.id " +
                      "JOIN routes r ON t.company_id = r.company_id " +
                      "WHERE r.id = ? AND t.is_available = true";
        
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, routeId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transport transport = mapResultSetToTransport(rs);
                    transports.add(transport);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching transports by route id", e);
        }
        
        return transports;
    }
    
    @Override
    public List<Transport> getAvailableTransports() {
        List<Transport> transports = new ArrayList<>();
        String query = "SELECT t.*, tt.name as type_name FROM transports t JOIN transport_types tt ON t.type_id = tt.id WHERE t.is_available = true";
        
        try (PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Transport transport = mapResultSetToTransport(rs);
                transports.add(transport);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching available transports", e);
        }
        
        return transports;
    }
    
    private Transport mapResultSetToTransport(ResultSet rs) throws Exception {
        Transport transport = new Transport();
        transport.setId(rs.getInt("id"));
        transport.setName(rs.getString("name"));
        transport.setTypeId(rs.getInt("type_id"));
        transport.setType(rs.getString("type_name"));
        transport.setCapacity(rs.getInt("capacity"));
        transport.setCompanyId(rs.getInt("company_id"));
        transport.setLicensePlate(rs.getString("license_plate"));
        transport.setAvailable(rs.getBoolean("is_available"));
        
        return transport;
    }
} 