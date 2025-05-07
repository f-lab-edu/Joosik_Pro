package com.joopro.Joosik_Pro.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDBConfig {

    private static final String TOKEN = "XSG_SqMvwNIiK43pHyRVzeqr58Ax11Vway8InrTEWA_cX_WtQpfQ2ugH_R1-DKBOiVYHvnK4Bo_61UQ82NHZag==";
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
