package com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventServiceV5;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joopro.Joosik_Pro.dto.ParticipationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaFirstComeEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Kafka Producer 클래스 (참여 요청 전송)
    public void sendParticipationRequest(Long stockId, Long memberId) {
        long startTimeNs = System.nanoTime(); // 측정 시작
        ParticipationMessage message = new ParticipationMessage(stockId, memberId, startTimeNs);
        try {
            String data = objectMapper.writeValueAsString(message);
            log.info("kafka 호출");
            kafkaTemplate.send("attend-event-participants", data)
                    .thenAccept(result -> log.info("Kafka 전송 성공: {}", result))
                    .exceptionally(ex -> {
                        log.error("Kafka 전송 실패", ex);
                        return null;
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Kafka JSON 직렬화 실패", e);
        }
    }

    public void saveParticipationRequest(String stockId){
        kafkaTemplate.send("save-event-participants", stockId);
    }

}
