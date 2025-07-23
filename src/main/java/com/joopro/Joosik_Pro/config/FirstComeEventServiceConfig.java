package com.joopro.Joosik_Pro.config;

import com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@RequiredArgsConstructor
public class FirstComeEventServiceConfig {

    private final ApplicationContext applicationContext;
    private final Environment env;

    @Bean
    public FirstComeEventService firstComeEventService() {
        String version = env.getProperty("firstcome.service.version", "v1");
        return applicationContext.getBean(version, FirstComeEventService.class);
    }
}
