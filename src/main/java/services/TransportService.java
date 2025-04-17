package services;

import entities.Transport;
import java.util.List;

public interface TransportService extends CRUD<Transport> {
    List<Transport> getTransportsByCompanyId(int companyId);
    List<Transport> getTransportsByRouteId(int routeId);
    List<Transport> getAvailableTransports();
} 