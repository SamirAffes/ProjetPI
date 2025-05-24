package tn.esprit.testpifx.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.esprit.testpifx.models.Team;

import java.sql.*;
import java.util.*;

/**
 * MySQL implementation of the TeamRepository interface.
 * Manages teams in a MySQL database.
 */
public class MySQLTeamRepository implements TeamRepository {
    private static final Logger logger = LoggerFactory.getLogger(MySQLTeamRepository.class);
    
    // Default connection parameters
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 3306;
    private static final String DEFAULT_DATABASE = "javafx";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "";
    
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    
    // SQL statements for team table
    private static final String CREATE_TEAMS_TABLE = 
            "CREATE TABLE IF NOT EXISTS teams (" +
            "team_id VARCHAR(36) PRIMARY KEY, " +
            "name VARCHAR(100) UNIQUE NOT NULL, " +
            "description TEXT, " +
            "created_by VARCHAR(36) NOT NULL" +
            ")";
    
    private static final String CREATE_TEAM_MEMBERS_TABLE = 
            "CREATE TABLE IF NOT EXISTS team_members (" +
            "team_id VARCHAR(36) NOT NULL, " +
            "user_id VARCHAR(36) NOT NULL, " +
            "PRIMARY KEY (team_id, user_id), " +
            "FOREIGN KEY (team_id) REFERENCES teams(team_id) ON DELETE CASCADE" +
            ")";
    
    private static final String INSERT_TEAM = 
            "INSERT INTO teams (team_id, name, description, created_by) " +
            "VALUES (?, ?, ?, ?)";
    
    private static final String UPDATE_TEAM = 
            "UPDATE teams SET name = ?, description = ? WHERE team_id = ?";
    
    private static final String SELECT_TEAM_BY_ID = 
            "SELECT * FROM teams WHERE team_id = ?";
    
    private static final String SELECT_TEAM_BY_NAME = 
            "SELECT * FROM teams WHERE name = ?";
    
    private static final String SELECT_ALL_TEAMS = 
            "SELECT * FROM teams";
    
    private static final String DELETE_TEAM = 
            "DELETE FROM teams WHERE team_id = ?";
    
    private static final String EXISTS_BY_NAME = 
            "SELECT COUNT(*) FROM teams WHERE name = ?";
    
    private static final String EXISTS_BY_ID = 
            "SELECT COUNT(*) FROM teams WHERE team_id = ?";
    
    private static final String SELECT_TEAMS_BY_CREATOR = 
            "SELECT * FROM teams WHERE created_by = ?";
    
    private static final String SELECT_TEAMS_BY_MEMBER = 
            "SELECT t.* FROM teams t INNER JOIN team_members tm ON t.team_id = tm.team_id WHERE tm.user_id = ?";
    
    private static final String ADD_TEAM_MEMBER = 
            "INSERT INTO team_members (team_id, user_id) VALUES (?, ?)";
    
    private static final String REMOVE_TEAM_MEMBER = 
            "DELETE FROM team_members WHERE team_id = ? AND user_id = ?";
    
    private static final String SELECT_TEAM_MEMBERS = 
            "SELECT user_id FROM team_members WHERE team_id = ?";
    
    private static final String IS_TEAM_MEMBER = 
            "SELECT COUNT(*) FROM team_members WHERE team_id = ? AND user_id = ?";
    
    /**
     * Constructor with default connection parameters
     */
    public MySQLTeamRepository() {
        this(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_DATABASE, DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }
    
    /**
     * Constructor with custom connection parameters
     */
    public MySQLTeamRepository(String host, int port, String database, String username, String password) {
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
            
            // Create tables if they don't exist
            stmt.execute(CREATE_TEAMS_TABLE);
            stmt.execute(CREATE_TEAM_MEMBERS_TABLE);
            logger.info("MySQL database initialized successfully for teams");
            
        } catch (SQLException e) {
            logger.error("Error initializing MySQL database for teams: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize MySQL database for teams", e);
        }
    }
    
    private Connection getConnection() throws SQLException {
        String url = String.format("jdbc:mysql://%s:%d/%s?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC", 
                host, port, database);
        return DriverManager.getConnection(url, username, password);
    }
    
    @Override
    public void save(Team team) {
        // Check if the team exists in the database
        boolean exists = team.getTeamId() != null && existsById(team.getTeamId());
        if (!exists) {
            // New team - generate ID if needed and insert
            if (team.getTeamId() == null) {
                team.setTeamId(UUID.randomUUID().toString());
            }
            insertTeam(team);
        } else {
            // Existing team - update
            updateTeam(team);
        }
        logger.info("Team saved: {}", team.getName());
    }
    
    private void insertTeam(Team team) {
        try (Connection conn = getConnection()) {
            // Begin a transaction to ensure consistency
            conn.setAutoCommit(false);
            
            try {
                // First insert the team without members
                try (PreparedStatement pstmt = conn.prepareStatement(INSERT_TEAM)) {
                    pstmt.setString(1, team.getTeamId());
                    pstmt.setString(2, team.getName());
                    pstmt.setString(3, team.getDescription());
                    pstmt.setString(4, team.getCreatedBy());
                    pstmt.executeUpdate();
                }
                
                // Then save team members after successful team insertion
                if (!team.getMemberIds().isEmpty()) {
                    saveTeamMembers(conn, team);
                }
                
                // Commit the transaction
                conn.commit();
            } catch (SQLException e) {
                // If there's an error, roll back the transaction
                conn.rollback();
                throw e;
            } finally {
                // Reset auto-commit
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            logger.error("Error inserting team: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to insert team", e);
        }
    }
    
    private void updateTeam(Team team) {
        try (Connection conn = getConnection()) {
            // Begin transaction
            conn.setAutoCommit(false);
            
            try {
                // Update team details
                try (PreparedStatement pstmt = conn.prepareStatement(UPDATE_TEAM)) {
                    pstmt.setString(1, team.getName());
                    pstmt.setString(2, team.getDescription());
                    pstmt.setString(3, team.getTeamId());
                    pstmt.executeUpdate();
                }
                
                // Update team members (delete all existing and re-insert)
                try (PreparedStatement deleteStmt = conn.prepareStatement(
                        "DELETE FROM team_members WHERE team_id = ?")) {
                    deleteStmt.setString(1, team.getTeamId());
                    deleteStmt.executeUpdate();
                }
                
                if (!team.getMemberIds().isEmpty()) {
                    saveTeamMembers(conn, team);
                }
                
                // Commit the transaction
                conn.commit();
            } catch (SQLException e) {
                // Roll back the transaction if an error occurs
                conn.rollback();
                throw e;
            } finally {
                // Reset auto-commit mode
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            logger.error("Error updating team: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update team", e);
        }
    }
    
    private void saveTeamMembers(Connection conn, Team team) throws SQLException {
        if (team.getMemberIds().isEmpty()) {
            return;
        }

        // Validate that the team_id exists in the teams table
        try (PreparedStatement validateStmt = conn.prepareStatement(EXISTS_BY_ID)) {
            validateStmt.setString(1, team.getTeamId());
            ResultSet rs = validateStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                throw new SQLException("Team ID " + team.getTeamId() + " does not exist in the teams table.");
            }
        }

        try (PreparedStatement pstmt = conn.prepareStatement(ADD_TEAM_MEMBER)) {
            for (String userId : team.getMemberIds()) {
                pstmt.setString(1, team.getTeamId());
                pstmt.setString(2, userId);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
    
    @Override
    public Optional<Team> findById(String teamId) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_TEAM_BY_ID)) {
            
            pstmt.setString(1, teamId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Team team = resultSetToTeam(rs);
                loadTeamMembers(conn, team);
                return Optional.of(team);
            }
            
        } catch (SQLException e) {
            logger.error("Error finding team by ID: {}", e.getMessage(), e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<Team> findByName(String name) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_TEAM_BY_NAME)) {
            
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Team team = resultSetToTeam(rs);
                loadTeamMembers(conn, team);
                return Optional.of(team);
            }
            
        } catch (SQLException e) {
            logger.error("Error finding team by name: {}", e.getMessage(), e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Team> findAll() {
        List<Team> teams = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_TEAMS)) {
            
            while (rs.next()) {
                Team team = resultSetToTeam(rs);
                loadTeamMembers(conn, team);
                teams.add(team);
            }
            
        } catch (SQLException e) {
            logger.error("Error finding all teams: {}", e.getMessage(), e);
        }
        
        return teams;
    }
    
    @Override
    public void deleteById(String teamId) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_TEAM)) {
            
            pstmt.setString(1, teamId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            logger.error("Error deleting team: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public boolean existsByName(String name) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(EXISTS_BY_NAME)) {
            
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            logger.error("Error checking if team exists by name: {}", e.getMessage(), e);
        }
        
        return false;
    }
    
    @Override
    public List<Team> findByCreatedBy(String userId) {
        List<Team> teams = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_TEAMS_BY_CREATOR)) {
            
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Team team = resultSetToTeam(rs);
                loadTeamMembers(conn, team);
                teams.add(team);
            }
            
        } catch (SQLException e) {
            logger.error("Error finding teams by creator: {}", e.getMessage(), e);
        }
        
        return teams;
    }
    
    @Override
    public List<Team> findByMemberId(String userId) {
        List<Team> teams = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_TEAMS_BY_MEMBER)) {
            
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Team team = resultSetToTeam(rs);
                loadTeamMembers(conn, team);
                teams.add(team);
            }
            
        } catch (SQLException e) {
            logger.error("Error finding teams by member: {}", e.getMessage(), e);
        }
        
        return teams;
    }
    
    @Override
    public boolean addMember(String teamId, String userId) {
        if (isMember(teamId, userId)) {
            return false; // Already a member
        }
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(ADD_TEAM_MEMBER)) {
            
            pstmt.setString(1, teamId);
            pstmt.setString(2, userId);
            pstmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            logger.error("Error adding team member: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean removeMember(String teamId, String userId) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(REMOVE_TEAM_MEMBER)) {
            
            pstmt.setString(1, teamId);
            pstmt.setString(2, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.error("Error removing team member: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Set<String> getMembers(String teamId) {
        Set<String> members = new HashSet<>();
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_TEAM_MEMBERS)) {
            
            logger.debug("Getting members for team ID: {}", teamId);
            pstmt.setString(1, teamId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String userId = rs.getString("user_id");
                members.add(userId);
                logger.debug("Found team member: {}", userId);
            }
            
            logger.debug("Total team members found: {}", members.size());
            
        } catch (SQLException e) {
            logger.error("Error getting team members: {}", e.getMessage(), e);
        }
        
        return members;
    }
    
    @Override
    public boolean isMember(String teamId, String userId) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(IS_TEAM_MEMBER)) {
            
            pstmt.setString(1, teamId);
            pstmt.setString(2, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            logger.error("Error checking if user is team member: {}", e.getMessage(), e);
        }
        
        return false;
    }
    
    @Override
    public boolean existsById(String teamId) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(EXISTS_BY_ID)) {
            
            pstmt.setString(1, teamId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            logger.error("Error checking if team exists by ID: {}", e.getMessage(), e);
        }
        
        return false;
    }
    
    private Team resultSetToTeam(ResultSet rs) throws SQLException {
        Team team = new Team();
        team.setTeamId(rs.getString("team_id"));
        team.setName(rs.getString("name"));
        team.setDescription(rs.getString("description"));
        team.setCreatedBy(rs.getString("created_by"));
        return team;
    }
    
    private void loadTeamMembers(Connection conn, Team team) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(SELECT_TEAM_MEMBERS)) {
            pstmt.setString(1, team.getTeamId());
            ResultSet rs = pstmt.executeQuery();
            
            Set<String> members = new HashSet<>();
            while (rs.next()) {
                members.add(rs.getString("user_id"));
            }
            
            team.setMemberIds(members);
        }
    }
}