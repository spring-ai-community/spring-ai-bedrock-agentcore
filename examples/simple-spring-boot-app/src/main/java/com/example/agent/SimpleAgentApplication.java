package com.example.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
public class SimpleAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleAgentApplication.class, args);
    }
}
