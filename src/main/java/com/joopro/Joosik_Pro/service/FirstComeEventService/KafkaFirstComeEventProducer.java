package com.joopro.Joosik_Pro.service.FirstComeEventService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KafkaFirstComeEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Kafka Producer 클래스 (참여 요청 전송)
    public void sendParticipationRequest(Long stockId, Long memberId) {
        Map<String, Object> sendMap = new HashMap<>();
        sendMap.put("stockId", stockId);
        sendMap.put("memberId", memberId);
        try {
            String data = objectMapper.writeValueAsString(sendMap);
            kafkaTemplate.send("first-come-event", memberId.toString(), data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Kafka JSON 직렬화 실패", e);
        }
    }

    public void saveParticipationRequest(String stockId){
        kafkaTemplate.send("save-event-participants", stockId);
    }

}
