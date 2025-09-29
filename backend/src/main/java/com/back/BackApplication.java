package com.back;

import com.google.api.client.util.Value;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
public class BackApplication {

    @Value("${GOOGLE_APPLICATION_CREDENTIALS}")
    private String googleCredentialsPath;

    public static void main(String[] args) {
        SpringApplication.run(BackApplication.class, args);
    }

    @PostConstruct
    public void setGoogleCredentialsEnv() {
        // 구글 SDK가 인식할 수 있도록 시스템 속성에 넣어줌
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", googleCredentialsPath);
    }

}
