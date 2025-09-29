package com.example.rtbAttribution.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;

@Configuration
@Slf4j
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    private final ElasticsearchProperties properties;

    public ElasticsearchConfig(ElasticsearchProperties properties) {
        this.properties = properties;
    }

    @Override
    public ClientConfiguration clientConfiguration() {
        log.info("Creating ClientConfiguration with uris={}, username={}",
                properties.getUris(), properties.getUsername());

        return ClientConfiguration.builder()
                .connectedTo(properties.getUris().toArray(new String[0]))
                .build();
    }
}
