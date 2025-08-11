package com.joopro.Joosik_Pro.config;

import com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class FirstComeEventServiceConfig {

    @Value("${firstcome.service.version:v1}")  // 기본값 설정 가능
    private String serviceVersion;

    @Bean
    public FirstComeEventService firstComeEventService(Map<String, FirstComeEventService> serviceMap) {
        FirstComeEventService selected = serviceMap.get(serviceVersion);
        if (selected == null) {
            throw new IllegalArgumentException("존재하지 않는 이벤트 서비스 구현체: " + serviceVersion);
        }
        return selected;
    }
}
