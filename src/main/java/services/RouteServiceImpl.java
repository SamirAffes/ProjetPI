package services;

import entities.Route;
import utils.DbContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RouteServiceImpl implements RouteService {
    private static final Logger LOGGER = Logger.getLogger(RouteServiceImpl.class.getName());
    private final Connection con = DbContext.getInstance().getConn();
    
    @Override
    public void ajouter(Route r) {
        String query = "INSERT INTO routes (origin, destination, distance, estimated_duration, base_price, company_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getOrigin());
            ps.setString(2, r.getDestination());
            ps.setDouble(3, r.getDistance());
            ps.setInt(4, r.getEstimatedDuration());
            ps.setDouble(5, r.getBasePrice());
            ps.setInt(6, r.getCompanyId());
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        r.setId(rs.getInt(1));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding route", e);
        }
    }

    @Override
    public void supprimer(Route r) {
        supprimer(r.getId());
    }
    
    public boolean supprimer(int id) {
        String query = "DELETE FROM routes WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting route", e);
            return false;
        }
    }

    @Override
    public void modifier(Route r) {
        String query = "UPDATE routes SET origin = ?, destination = ?, distance = ?, estimated_duration = ?, base_price = ?, company_id = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, r.getOrigin());
            ps.setString(2, r.getDestination());
            ps.setDouble(3, r.getDistance());
            ps.setInt(4, r.getEstimatedDuration());
            ps.setDouble(5, r.getBasePrice());
            ps.setInt(6, r.getCompanyId());
            ps.setInt(7, r.getId());
            
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating route", e);
        }
    }
    
    @Override
    public Route afficher(int id) {
        return getById(id);
    }
    
    @Override
    public List<Route> afficher_tout() {
        return getAll();
    }

    public List<Route> getAll() {
        List<Route> routes = new ArrayList<>();
        String query = "SELECT * FROM routes";
        
        try (PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Route route = new Route();
                route.setId(rs.getInt("id"));
                route.setOrigin(rs.getString("origin"));
                route.setDestination(rs.getString("destination"));
                route.setDistance(rs.getDouble("distance"));
                route.setEstimatedDuration(rs.getInt("estimated_duration"));
                route.setBasePrice(rs.getDouble("base_price"));
                route.setCompanyId(rs.getInt("company_id"));
                
                routes.add(route);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching all routes", e);
        }
        
        return routes;
    }

    public Route getById(int id) {
        String query = "SELECT * FROM routes WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Route route = new Route();
                    route.setId(rs.getInt("id"));
                    route.setOrigin(rs.getString("origin"));
                    route.setDestination(rs.getString("destination"));
                    route.setDistance(rs.getDouble("distance"));
                    route.setEstimatedDuration(rs.getInt("estimated_duration"));
                    route.setBasePrice(rs.getDouble("base_price"));
                    route.setCompanyId(rs.getInt("company_id"));
                    
                    return route;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching route by id", e);
        }
        
        return null;
    }

    @Override
    public List<Route> getRoutesByCompanyId(int companyId) {
        List<Route> routes = new ArrayList<>();
        String query = "SELECT * FROM routes WHERE company_id = ?";
        
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, companyId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Route route = new Route();
                    route.setId(rs.getInt("id"));
                    route.setOrigin(rs.getString("origin"));
                    route.setDestination(rs.getString("destination"));
                    route.setDistance(rs.getDouble("distance"));
                    route.setEstimatedDuration(rs.getInt("estimated_duration"));
                    route.setBasePrice(rs.getDouble("base_price"));
                    route.setCompanyId(rs.getInt("company_id"));
                    
                    routes.add(route);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching routes by company id", e);
        }
        
        return routes;
    }

    @Override
    public double calculateDistance(String origin, String destination) {
        // In a real application, this would use a mapping API or algorithm
        // For demo purposes, return a fixed value
        return 50.0; // 50 km
    }
} 