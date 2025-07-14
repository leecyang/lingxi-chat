package com.lingxi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 灵犀智学应用程序启动类
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.lingxi.repository")
@EnableCaching
@EnableAsync
@EnableScheduling
public class LingxiChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(LingxiChatApplication.class, args);
    }
}