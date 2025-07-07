package com.joopro.Joosik_Pro.service.FirstComeEventService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.dto.ParticipationMessage;
import com.joopro.Joosik_Pro.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaFirstComeEventConsumer {

    private final StringRedisTemplate stringRedisTemplate;
    private final StockRepository stockRepository;
    private final AsyncSaveService asyncSaveService;
    private final TaskExecutor taskExecutor;
    private final KafkaFirstComeEventProducer kafkaFirstComeEventProducer;
    private static final int MAX_PARTICIPANTS = 100;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "first-come-event", groupId = "event-consumer-group")
    public void handleParticipation(String message) {
        // 메시지 파싱
        ParticipationMessage data = parseMessage(message);
        Long stockId = data.getStockId();
        Long memberId = data.getMemberId();

        String key = "event:" + stockId + ":participants";

        // 중복 확인
        Double score = stringRedisTemplate.opsForZSet().score(key, memberId.toString());
        if (score != null) return;

        // 현재 인원 확인
        Long count = stringRedisTemplate.opsForZSet().zCard(key);
        if (count != null && count >= MAX_PARTICIPANTS) return;

        long order = count != null ? count + 1 : 1;
        Boolean success = stringRedisTemplate.opsForZSet().add(key, memberId.toString(), order);
        if (Boolean.FALSE.equals(success)) return;

        if (order == MAX_PARTICIPANTS) {
            kafkaFirstComeEventProducer.saveParticipationRequest(stockId.toString());
        }
    }


    @KafkaListener(topics = "save-event-participants", groupId = "participant-saver-group")
    public void consumeStockId(String stockIdStr) {
        Long stockId = Long.valueOf(stockIdStr);
        String key = "event:" + stockId + ":participants";

        Set<ZSetOperations.TypedTuple<String>> orderedParticipants =
                stringRedisTemplate.opsForZSet().rangeWithScores(key, 0, -1);

        if (orderedParticipants == null || orderedParticipants.isEmpty()) {
            log.warn("No participants found for stockId: {}", stockId);
            return;
        }

        Stock stock = stockRepository.findStockById(stockId);
        for (ZSetOperations.TypedTuple<String> tuple : orderedParticipants) {
            Long memberId = Long.valueOf(Objects.requireNonNull(tuple.getValue()));
            int order = Objects.requireNonNull(tuple.getScore()).intValue();
            taskExecutor.execute(() -> asyncSaveService.saveParticipantTransactional(stock, memberId, order));
        }
    }

    private ParticipationMessage parseMessage(String message) {
        try {
            return objectMapper.readValue(message,ParticipationMessage.class);
        } catch (Exception e) {
            throw new RuntimeException("Kafka JSON 역직렬화 실패", e);
        }
    }

}
