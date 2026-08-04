package com.laybhari.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials-path:firebase-service-account.json}")
    private String credentialsPath;

    @PostConstruct
    public void initializeFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try {
            InputStream serviceAccountStream = null;

            Resource classpathResource = new ClassPathResource(credentialsPath);
            if (classpathResource.exists()) {
                serviceAccountStream = classpathResource.getInputStream();
            } else {
                Resource fileResource = new FileSystemResource(credentialsPath);
                if (fileResource.exists()) {
                    serviceAccountStream = fileResource.getInputStream();
                }
            }

            if (serviceAccountStream != null) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("🔥 Firebase Admin SDK initialized successfully.");
            } else {
                log.warn("⚠️ Firebase service account file not found at '{}'. Firebase ID token verification will be available once credentials are provided.", credentialsPath);
            }
        } catch (Exception e) {
            log.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage(), e);
        }
    }
}
