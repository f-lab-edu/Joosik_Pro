package com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventServiceV5;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joopro.Joosik_Pro.dto.ParticipationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaFirstComeEventConsumer {

    private final JobLauncher jobLauncher;
    private final StringRedisTemplate stringRedisTemplate;
    private final Job saveParticipantsJob;
    private final KafkaFirstComeEventProducer kafkaFirstComeEventProducer;
    private static final int MAX_PARTICIPANTS = 100;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "attend-event-participants", groupId = "participant-attend-group")
    public void handleParticipation(String message) {
        log.info("호출됨");
        // 메시지 파싱
        ParticipationMessage data = parseMessage(message);
        Long stockId = data.getStockId();
        Long memberId = data.getMemberId();

        String key = "event:" + stockId + ":participants";

        // 중복 확인
        Double score = stringRedisTemplate.opsForZSet().score(key, memberId.toString());
        if (score != null){
            log.info("여기서 리턴1");
            return;
        }


        // 현재 인원 확인
        Long count = stringRedisTemplate.opsForZSet().zCard(key);
        if (count != null && count >= MAX_PARTICIPANTS){
            log.info("여기서 리턴2");
            return;
        }

        long order = count != null ? count + 1 : 1;
        log.info("stockId : {}, order : {}", stockId, count);
        Boolean success = stringRedisTemplate.opsForZSet().add(key, memberId.toString(), order);
        if (Boolean.FALSE.equals(success)){
            log.info("여기서 리턴3");
            return;
        }

        if (order == MAX_PARTICIPANTS) {
            log.info("저장 호출");
            kafkaFirstComeEventProducer.saveParticipationRequest(stockId.toString());
        }
    }


    @KafkaListener(topics = "save-event-participants", groupId = "participant-saver-group")
    public void consumeStockId(String stockIdStr) {
        Long stockId = Long.valueOf(stockIdStr);

        try {
            JobParameters parameters = new JobParametersBuilder()
                    .addString("stockId", stockId.toString())
                    .addLong("time", System.currentTimeMillis()) // 중복 실행 방지용
                    .toJobParameters();
            jobLauncher.run(saveParticipantsJob, parameters);
            log.info("batch 호출됨");
        } catch (Exception e) {
            log.error(" 배치 작업 실행 중 오류 발생", e);
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
