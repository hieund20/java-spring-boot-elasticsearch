package com.elasticsearch.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.elasticsearch.elasticsearch.helper.Indices;
import com.elasticsearch.elasticsearch.helper.Util;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

@Service
public class IndexService {

    private final List<String> INDICES_TO_CREATE = List.of(Indices.VEHICLE_INDEX);

    private final ElasticsearchClient client;

    private static final Logger LOG = LoggerFactory.getLogger(IndexService.class);

    @Autowired
    public IndexService(ElasticsearchClient client) {
        this.client = client;
    }

    @PostConstruct
    public void tryToCreateIndices() {
        reCreateIndices(false);
    }

    public void reCreateIndices(final boolean deleteExisting) {
        final String settings = Util.loadAsString("static/es-settings.json");

        for (final String indexName: INDICES_TO_CREATE) {
            try {
                boolean indexExists = client.indices().exists(
                        b -> b.index(indexName)).value();
                if (indexExists) {
                    if (!deleteExisting) {
                        continue;
                    }
                }

                final String mappings = Util.loadAsString(
                        "static/mappings/" + indexName + ".json");
                if (settings == null || mappings == null) {
                    LOG.error("Failed to create index with name '{}'", indexName);
                    continue;
                }

                client.indices().create(c ->
                        c.index(indexName)
                                .settings(s -> s.withJson(new StringReader(settings)))
                                .mappings(m -> m.withJson(new StringReader(mappings)))
                );

                LOG.info("Successfully created index '{}'", indexName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
