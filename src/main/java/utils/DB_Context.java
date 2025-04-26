package utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import io.github.cdimascio.dotenv.Dotenv;


@Slf4j
public class db_context{
    private final Dotenv dotenv = Dotenv.load();
    private final String USER = dotenv.get("DB_USER");
    private final String PASSWORD = dotenv.get("DB_PASSWORD");
    private final String URL = dotenv.get("DB_URL");

    private static volatile db_context instance;

    @Getter
    private Connection conn;

    private db_context() {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            log.info("Connected to database");
            initTables();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }


    public static db_context getInstance() {
        db_context temp = instance;
        if (temp == null) {
            synchronized (db_context.class) {
                temp = instance;
                if (temp == null) instance = temp= new db_context();
            }
        }
        return temp;
    }
    
    /**
     * Initialise les tables de la base de données si elles n'existent pas
     */
    private void initTables() {
        try {
            Statement stmt = conn.createStatement();
            
            // Création de la table Reclamation si elle n'existe pas
            String createTableReclamation = "CREATE TABLE IF NOT EXISTS Reclamation (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "type VARCHAR(100) NOT NULL, " +
                "description TEXT, " +
                "date DATE NOT NULL, " +
                "etat ENUM('EN_ATTENTE', 'TRAITE', 'REFUSE', 'EN_COURS', 'TERMINE') NOT NULL DEFAULT 'EN_ATTENTE', " +
                "user_id INT, " +
                "chauffeur_id INT, " +
                "organisme_id INT" +
                ")";
            
            stmt.executeUpdate(createTableReclamation);
            log.info("Table Reclamation vérifiée/créée avec succès");
            
        } catch (SQLException e) {
            log.error("Erreur lors de l'initialisation des tables: " + e.getMessage());
        }
    }
}
