package com.elasticsearch.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.elasticsearch.elasticsearch.document.Vehicle;
import com.elasticsearch.elasticsearch.helper.Indices;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class VehicleService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(VehicleService.class);

    private final ElasticsearchClient client;

    @Autowired
    public VehicleService(ElasticsearchClient client) {
        this.client = client;
    }

    public Boolean index(final Vehicle vehicle) {
        try {
            IndexResponse response = client.index(i ->
                    i.index(Indices.VEHICLE_INDEX)
                            .id(vehicle.getId())
                            .document(vehicle));

            return response.result().name().equals("Created")
                    || response.result().name().equals("Updated");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    public Vehicle getById(final String vehicleId) {
        try {
            var response = client.get(g -> g
                            .index(Indices.VEHICLE_INDEX)
                            .id(vehicleId),
                    Vehicle.class
            );

            if (!response.found()) {
                return null;
            }

            return response.source();

        } catch (IOException e) {
            LOGGER.error("Error getting vehicle by ID: {}", vehicleId, e);
            return null;
        }
    }
}
