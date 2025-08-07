package com.dti.drone_delivery_simulator.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.dti.drone_delivery_simulator.enums.RouteStatus;
import com.dti.drone_delivery_simulator.exception.RouteNotFoundException;
import com.dti.drone_delivery_simulator.exception.RouteStatusException;
import com.dti.drone_delivery_simulator.model.Route;

@Repository
public class InMemoryRouteRepository implements RouteRepository{
    
    private final Map<Long, Route> routes = new LinkedHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Route save(Route route) {
        if (route.getId() == null) {
            route.setId(idGenerator.getAndIncrement());
        }
        routes.put(route.getId(), route);
        return route;
    }

    @Override
    public Optional<Route> findById(Long id) {
        return Optional.ofNullable(routes.get(id));
    }

    @Override
    public List<Route> findAll() {
        return new ArrayList<>(routes.values());
    }

    @Override
    public Route updateStatus(Long id, RouteStatus status) {
        if (status == null) {
            throw new RouteStatusException("Status não pode ser nulo");
        }
        
        Route route = routes.get(id);
        if (route != null) {
            route.setStatus(status);
            return route;
        }
        throw new RouteNotFoundException("Rota não encontrada");
    }
}
