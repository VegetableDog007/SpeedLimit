package com.rate_controller.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class RateControlConfig {

    /**
     * 读取限流脚本
     * 以后这里有不同的限流策略的lua脚本可以更改id指定即可
     * @return
     */
    @Bean("redisLuaScript")
    @Lazy
    public DefaultRedisScript<Number> redisLuaScript() {
        DefaultRedisScript<Number> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua_scripts/rateLimit.lua")));
        redisScript.setResultType(Number.class);
        return redisScript;
    }

    /**
     * 返回一个redisTemplate的操作对象
     * @return
     */
    @Bean("redisTemplate")
    @Lazy
    public RedisTemplate<String, Object> redisTemplate(@Autowired() @Qualifier("redisConnectionFactory") LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

}
