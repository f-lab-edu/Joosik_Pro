package com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventServiceV5;

import com.joopro.Joosik_Pro.domain.FirstComeEventParticipation;
import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.dto.ParticipantDto;
import com.joopro.Joosik_Pro.repository.FirstComeEventRepository.FirstComeEventRepositoryV1;
import com.joopro.Joosik_Pro.repository.MemberRepository;
import com.joopro.Joosik_Pro.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ParticipantBatchConfig {

    private final StockRepository stockRepository;
    private final MemberRepository memberRepository;
    private final FirstComeEventRepositoryV1 repository;
    private final StringRedisTemplate stringRedisTemplate;

    @Bean
    public Job saveParticipantsJob(JobRepository jobRepository, Step saveParticipantsStep) {
        log.info("스프링 잡 호출");
        return new JobBuilder("saveParticipantsJob", jobRepository)
                .start(saveParticipantsStep)
                .build();
    }

    @Bean
    public Step saveParticipantsStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     @Qualifier("participantReader") ItemReader<ParticipantDto> participantReader) {
        return new StepBuilder("saveParticipantsStep", jobRepository)
                .<ParticipantDto, FirstComeEventParticipation>chunk(100, transactionManager)
                .reader(participantReader) // stockId는 StepScope에서 주입됨
                .processor(participantProcessor())
                .writer(participantWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<ParticipantDto> participantReader(
            @Value("#{jobParameters['stockId'] ?: '0'}") String stockIdStr
    ) {
        Long stockId = Long.valueOf(stockIdStr);

        if (stockId == 0) {
            return new ListItemReader<>(Collections.emptyList());
        }

        String key = "event:" + stockId + ":participants";

        Set<ZSetOperations.TypedTuple<String>> orderedParticipants =
                stringRedisTemplate.opsForZSet().rangeWithScores(key, 0, -1);

        List<ParticipantDto> list = orderedParticipants == null ? Collections.emptyList() :
                orderedParticipants.stream()
                        .map(tuple -> new ParticipantDto(
                                stockId,
                                Long.valueOf(Objects.requireNonNull(tuple.getValue())),
                                Objects.requireNonNull(tuple.getScore()).intValue()
                        ))
                        .toList();

        return new ListItemReader<>(list);
    }

    @Bean
    public ItemProcessor<ParticipantDto, FirstComeEventParticipation> participantProcessor() {
        return dto -> {
            Stock stock = stockRepository.findStockById(dto.getStockId());
            Member member = memberRepository.findOne(dto.getMemberId());
            return FirstComeEventParticipation.firstComeEventParticipation(stock, member, dto.getOrder());
        };
    }

    @Bean
    public ItemWriter<FirstComeEventParticipation> participantWriter() {
        return (chunk) -> {
            log.info("Writer received {} items", chunk.getItems().size());
            repository.saveAll((List<FirstComeEventParticipation>) chunk.getItems());
        };
    }
}

