package com.dti.drone_delivery_simulator.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.dti.drone_delivery_simulator.model.Route;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InMemoryRouteRepository implements RouteRepository{
    
    private final Map<Long, Route> routes;
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
}
