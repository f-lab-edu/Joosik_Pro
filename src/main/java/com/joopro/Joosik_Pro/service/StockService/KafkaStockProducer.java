package com.joopro.Joosik_Pro.service.StockService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joopro.Joosik_Pro.dto.StockMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaStockProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${stock.kafka.topic}")
    private String topic;

    public void sendStockData(StockMessage stockMessage) {
        try {
            String json = objectMapper.writeValueAsString(stockMessage);
            kafkaTemplate.send(topic, json);
        } catch (JsonProcessingException e) {
            log.error("Kafka 메시지 JSON 직렬화 실패", e);
        }
    }
}
