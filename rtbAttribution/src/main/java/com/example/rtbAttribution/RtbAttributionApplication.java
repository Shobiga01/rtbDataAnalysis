package com.example.rtbAttribution;


import com.example.rtbAttribution.Service.AggregationService;
import com.example.rtbAttribution.Service.DatabaseService;
import com.example.rtbAttribution.Service.ElasticsearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableScheduling
@EnableAsync
@EnableConfigurationProperties
@RequiredArgsConstructor
@SpringBootApplication(scanBasePackages = "com.example.rtbAttribution")
@Slf4j
public class RtbAttributionApplication {

	private final DatabaseService databaseService;
	private final AggregationService aggregationService;
	private final ElasticsearchService elasticsearchService;

	public static void main(String[] args) {
		SpringApplication.run(RtbAttributionApplication.class, args);
	}

	@Bean
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("RTBAsync-");
		executor.initialize();
		return executor;
	}

	@Bean
	public ApplicationRunner initRunner() {
		return args -> {
			log.info("Starting RTB Aggregator");

			if (!databaseService.loadAllData()) {
				log.error("MySQL initial read failed. Exiting.");
				System.exit(1);
			}

			log.info("Console output level is {}", log.isDebugEnabled() ? "DEBUG" : "INFO");
			log.info("Looking for Kafka brokers: {}", System.getenv().getOrDefault("RTBAGG_BROKERLIST", "kafka:9092"));
			log.info("Start writing to Elasticsearch {}, index base: {}",
					System.getenv().getOrDefault("RTBAGG_ESAGGURL", "http://192.168.2.102:9200"),
					System.getenv().getOrDefault("RTBAGG_ESAGGINDEX", "postbackeventsagg"));
			log.info("Read from DB host: {}", System.getenv().getOrDefault("RTBAGG_MYSQLHOST", "web_db"));
			log.info("Disable sending aggregation to Elasticsearch: {}",
					System.getenv().getOrDefault("RTBAGG_DISABLEAGGSEND", "false"));

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				log.warn("Shutdown detected. Flushing remaining aggregations.");
				aggregationService.flushAllAggregations();
				log.info("Shutdown complete. Final unprocessed events: {}", aggregationService.getUnprocessedCount());
				log.info("End RTB Aggregator");
			}));
		};
	}
}
