package com.elasticsearch.elasticsearch.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.annotation.PreDestroy;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.io.IOException;

@Configuration
public class ElasticSearchConfig {

    private RestClient restClient;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        try {
            // Chấp nhận tất cả chứng chỉ SSL (kể cả self-signed)
            final SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, (cert, authType) -> true)
                    .build();

            // Cấu hình xác thực với elastic user
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(
                            "elastic",
                            "duchieu"));

            RestClientBuilder builder = RestClient.builder(
                    new HttpHost("localhost", 9200, "https"))
                    .setHttpClientConfigCallback(
                            httpClientBuilder -> customizeHttpClient(
                                    httpClientBuilder,
                                    sslContext,
                                    credentialsProvider));

            restClient = builder.build();

            ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

            return new ElasticsearchClient(transport);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Elasticsearch client", e);
        }
    }

    private HttpAsyncClientBuilder customizeHttpClient(
            HttpAsyncClientBuilder builder,
            SSLContext sslContext,
            BasicCredentialsProvider credentialsProvider) {
        return builder
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier((hostname, session) -> true) // <- BỎ QUA xác minh tên máy chủ
                .setDefaultCredentialsProvider(credentialsProvider);
    }

    // Đóng client khi ứng dụng shutdown
    @PreDestroy
    public void close() throws IOException {
        if (restClient != null) {
            restClient.close();
        }
    }
}
