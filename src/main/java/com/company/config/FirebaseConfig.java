package com.company.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    @Value("${FIREBASE_SERVICE_ACCOUNT_PATH}")
    private String serviceAccountPath;

    @Value("${FIREBASE_SERVICE_ACCOUNT_BASE64}")
    private String serviceAccountBase64;

    @Value("${FIREBASE_DATABASE_URL}")
    private String databaseUrl;

    @PostConstruct
    public void initFirebase() throws IOException {
        //FileInputStream serviceAccount = new FileInputStream(serviceAccountPath);
        ByteArrayInputStream serviceAccount = new ByteArrayInputStream(
                Base64.getDecoder().decode(serviceAccountBase64));

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl(databaseUrl) // For Realtime Database
                // .setProjectId("<your-project-id>") // For Firestore (if needed)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }
}
