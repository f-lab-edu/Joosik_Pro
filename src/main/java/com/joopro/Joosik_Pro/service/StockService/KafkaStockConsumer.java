package com.joopro.Joosik_Pro.service.StockService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.joopro.Joosik_Pro.dto.StockMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaStockConsumer {

    private final ObjectMapper objectMapper;
    private final WriteApi writeApi;

    @KafkaListener(topics = "${stock.kafka.topic}", groupId = "stock-data-group")
    public void consume(String jsonMessage) {
        try {
            StockMessage stockMessage = objectMapper.readValue(jsonMessage, StockMessage.class);

            Point point = Point
                    .measurement("stock_price")
                    .addTag("code", stockMessage.getCode())
                    .addField("price", stockMessage.getPrice())
                    .addField("volume", stockMessage.getVolume())
                    .time(Instant.parse(stockMessage.getTimestamp()), WritePrecision.MS);

            writeApi.writePoint(point);
            log.info("InfluxDB 저장 완료: {}", stockMessage.getCode());

        } catch (Exception e) {
            log.error("Kafka 메시지 파싱 또는 InfluxDB 저장 오류", e);
        }
    }
}
