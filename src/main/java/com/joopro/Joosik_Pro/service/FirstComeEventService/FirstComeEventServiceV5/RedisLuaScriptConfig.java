package com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventServiceV5;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisLuaScriptConfig {
    @Bean
    public DefaultRedisScript<Long> tryParticipateScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScriptContent()); // 아래 함수 참고
        script.setResultType(Long.class);
        return script;
    }

    private String luaScriptContent() {
        return """
        local key = KEYS[1]
        local member = ARGV[1]
        local time = tonumber(ARGV[2])
        local max = tonumber(ARGV[3])
        if redis.call("ZSCORE", key, member) then return -1 end
        local count = redis.call("ZCARD", key)
        if count >= max then return 0 end
        redis.call("ZADD", key, time, member)
        if count + 1 == max then return 1 end
        return 2
    """;
    }
}
