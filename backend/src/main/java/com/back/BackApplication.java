package com.back;

import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        Path path = Paths.get(googleCredentialsPath).toAbsolutePath();
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", path.toString());
        System.out.println("*** Google credentials path set to: " + path);

        try (FileInputStream serviceAccountStream = new FileInputStream(path.toFile())) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);

            System.out.println("*** Google credentials loaded successfully!");
            System.out.println("*** Credentials type: " + credentials.getClass().getSimpleName());
        } catch (Exception e) {
            System.err.println("*** Failed to load Google credentials: " + e.getMessage());
        }
    }

}
