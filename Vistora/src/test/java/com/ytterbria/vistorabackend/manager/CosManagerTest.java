package com.ytterbria.vistorabackend.manager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties="spring.config.location=classpath:application-local.yml")
class CosManagerTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void redisOperations() {
        ValueOperations<String,String> valueOps = redisTemplate.opsForValue();
        valueOps.set("user","tester");
        String storedValue = valueOps.get("user");
        assertEquals("tester",storedValue);
    }
}