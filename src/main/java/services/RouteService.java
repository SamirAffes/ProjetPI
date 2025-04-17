package services;

import entities.Route;
import java.util.List;

public interface RouteService extends CRUD<Route> {
    List<Route> getRoutesByCompanyId(int companyId);
    double calculateDistance(String origin, String destination);
} 