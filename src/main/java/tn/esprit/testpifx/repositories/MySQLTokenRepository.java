package tn.esprit.testpifx.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.esprit.testpifx.models.VerificationToken;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing verification tokens in MySQL database.
 */
public class MySQLTokenRepository implements TokenRepository {
    private static final Logger logger = LoggerFactory.getLogger(MySQLTokenRepository.class);
    
    // Default connection parameters
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 3306;
    private static final String DEFAULT_DATABASE = "javafx";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "root";
    
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    
    // SQL statements for token table
    private static final String CREATE_TOKENS_TABLE = 
            "CREATE TABLE IF NOT EXISTS verification_tokens (" +
            "token VARCHAR(255) PRIMARY KEY, " +
            "user_id VARCHAR(36) NOT NULL, " +
            "token_type VARCHAR(50) NOT NULL, " +
            "expiry_date DATETIME NOT NULL, " +
            "used BOOLEAN NOT NULL DEFAULT FALSE, " +
            "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
            ")";
            
    private static final String INSERT_TOKEN = 
            "INSERT INTO verification_tokens (token, user_id, token_type, expiry_date, used) " +
            "VALUES (?, ?, ?, ?, ?)";
            
    private static final String SELECT_TOKEN_BY_TOKEN = 
            "SELECT * FROM verification_tokens WHERE token = ?";
            
    private static final String SELECT_TOKENS_BY_USER_AND_TYPE = 
            "SELECT * FROM verification_tokens WHERE user_id = ? AND token_type = ? AND used = FALSE " +
            "ORDER BY expiry_date DESC LIMIT 1";
            
    private static final String UPDATE_TOKEN_USED = 
            "UPDATE verification_tokens SET used = TRUE WHERE token = ?";
            
    private static final String DELETE_EXPIRED_TOKENS = 
            "DELETE FROM verification_tokens WHERE expiry_date < NOW()";
    
    /**
     * Constructor with default connection parameters
     */
    public MySQLTokenRepository() {
        this(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_DATABASE, DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }
    
    /**
     * Constructor with custom connection parameters
     */
    public MySQLTokenRepository(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create table if it doesn't exist
            stmt.execute(CREATE_TOKENS_TABLE);
            logger.info("MySQL database initialized successfully for tokens");
            
        } catch (SQLException e) {
            logger.error("Error initializing MySQL database for tokens: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize MySQL database for tokens", e);
        }
    }
    
    private Connection getConnection() throws SQLException {
        String url = String.format("jdbc:mysql://%s:%d/%s?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC", 
                host, port, database);
        return DriverManager.getConnection(url, username, password);
    }    @Override
    public void save(VerificationToken token) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            // Disable auto-commit for transaction
            conn.setAutoCommit(false);
            
            // Verify the user exists before trying to save the token
            String checkUserSql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkUserSql)) {
                checkStmt.setString(1, token.getUserId());
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next() && rs.getInt(1) == 0) {
                    logger.error("Attempted to save token for non-existent user: {}", token.getUserId());
                    throw new RuntimeException("User does not exist with ID: " + token.getUserId());
                }
            }
            
            pstmt = conn.prepareStatement(INSERT_TOKEN);
            
            pstmt.setString(1, token.getToken());
            pstmt.setString(2, token.getUserId());
            pstmt.setString(3, token.getTokenType().name());
            pstmt.setTimestamp(4, Timestamp.valueOf(token.getExpiryDate()));
            pstmt.setBoolean(5, token.isUsed());
            
            int rowsAffected = pstmt.executeUpdate();
            logger.info("Token saved: {} for user: {} with {} rows affected", 
                    token.getToken(), token.getUserId(), rowsAffected);
            
            // Commit the transaction
            conn.commit();
            
        } catch (SQLException e) {
            // Roll back on error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error rolling back transaction: {}", ex.getMessage(), ex);
                }
            }
            logger.error("Error saving token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save token", e);
        } finally {
            // Close resources
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    logger.error("Error closing statement: {}", e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection: {}", e.getMessage());
                }
            }
        }
    }

    @Override
    public Optional<VerificationToken> findByToken(String token) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_TOKEN_BY_TOKEN)) {
            
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                VerificationToken verificationToken = new VerificationToken(
                        rs.getString("user_id"),
                        VerificationToken.TokenType.valueOf(rs.getString("token_type"))
                );
                verificationToken.setToken(rs.getString("token"));
                verificationToken.setExpiryDate(rs.getTimestamp("expiry_date").toLocalDateTime());
                verificationToken.setUsed(rs.getBoolean("used"));
                
                return Optional.of(verificationToken);
            }
            
        } catch (SQLException e) {
            logger.error("Error finding token: {}", e.getMessage(), e);
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<VerificationToken> findActiveByUserAndType(String userId, VerificationToken.TokenType type) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_TOKENS_BY_USER_AND_TYPE)) {
            
            pstmt.setString(1, userId);
            pstmt.setString(2, type.name());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                VerificationToken token = new VerificationToken(
                        rs.getString("user_id"),
                        VerificationToken.TokenType.valueOf(rs.getString("token_type"))
                );
                token.setToken(rs.getString("token"));
                token.setExpiryDate(rs.getTimestamp("expiry_date").toLocalDateTime());
                token.setUsed(rs.getBoolean("used"));
                
                return Optional.of(token);
            }
            
        } catch (SQLException e) {
            logger.error("Error finding active token: {}", e.getMessage(), e);
        }
        
        return Optional.empty();
    }

    @Override
    public boolean markAsUsed(String token) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_TOKEN_USED)) {
            
            pstmt.setString(1, token);
            int rowsUpdated = pstmt.executeUpdate();
            
            return rowsUpdated > 0;
            
        } catch (SQLException e) {
            logger.error("Error marking token as used: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void deleteExpiredTokens() {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_EXPIRED_TOKENS)) {
            
            int rowsDeleted = pstmt.executeUpdate();
            logger.info("Deleted {} expired tokens", rowsDeleted);
            
        } catch (SQLException e) {
            logger.error("Error deleting expired tokens: {}", e.getMessage(), e);
        }
    }
}
