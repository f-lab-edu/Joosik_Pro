package com.joopro.Joosik_Pro.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDBConfig {

    private static final String TOKEN = "e6-KsfPLRr7EgnBAx0I3kTd3ZazkH9EzKlOzvo0flyPaJ79YewAGN6sLW68IHDcE45iK2SzIFz-AI_7S_f2mEA==";
    private static final String ORG = "Joosik_Pro";
    private static final String BUCKET = "Joosik_Pro";

    @Bean
    public InfluxDBClient influxDBClient() {
        String url = "http://localhost:8086";
        return InfluxDBClientFactory.create(url, TOKEN.toCharArray(), ORG, BUCKET);
    }

    @Bean
    public WriteApi writeApi(InfluxDBClient influxDBClient) {
        return influxDBClient.makeWriteApi();
    }

}
