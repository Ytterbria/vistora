package com.ytterbria.vistorabackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.ytterbria.vistorabackend")
@EnableAspectJAutoProxy(exposeProxy = true)
public class VistoraApplication {
    public static void main(String[] args) {
        SpringApplication.run(VistoraApplication.class, args);
    }
}
