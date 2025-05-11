package com.ytterbria.vistorabackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession
public class SessionConfig {
    // 默认使用 RedisConnectionFactory 自动注入的连接工厂
}