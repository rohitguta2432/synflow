package com.synflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SynFlowApplication {
    public static void main(String[] args) {
        SpringApplication.run(SynFlowApplication.class, args);
    }
}
