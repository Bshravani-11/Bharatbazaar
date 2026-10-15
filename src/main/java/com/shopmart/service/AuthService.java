package com.shopmart.service;

import com.shopmart.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AuthService - Authentication & User Account Service
 * 
 * Demonstrates:
 *   - User authentication and session management
 *   - User profile storage in Firebase Realtime Database (/users node)
 *   - In-memory caching for resilient offline/online login
 */
public class AuthService {

    private final Map<String, User> usersByEmail;
    private final HttpClient httpClient;
    private String databaseUrl;
    private boolean firebaseEnabled;

    public AuthService() {
        this.usersByEmail = new ConcurrentHashMap<>();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        loadConfig();
        seedDefaultUsers();
    }

    private void loadConfig() {
        Properties props = new Properties();
        File configFile = new File("firebase-config.properties");
        if (configFile.exists()) {
            try (InputStream in = new FileInputStream(configFile)) {
                props.load(in);
                this.firebaseEnabled = Boolean.parseBoolean(props.getProperty("firebase.enabled", "true"));
                this.databaseUrl = props.getProperty("firebase.database.url", "").trim();
                if (this.databaseUrl.endsWith("/")) {
                    this.databaseUrl = this.databaseUrl.substring(0, this.databaseUrl.length() - 1);
                }
            } catch (Exception ignored) {}
        }

        if (this.databaseUrl == null || this.databaseUrl.isEmpty()) {
            this.databaseUrl = "https://bharatbazaar-7797e-default-rtdb.firebaseio.com";
        }
    }

    /**
     * Seeds initial sample user for instant evaluation/testing
     */
    private void seedDefaultUsers() {
        User defaultUser = new User(
                UUID.randomUUID().toString(),
                "Sowmya",
                "sowmya@example.com",
                "password123",
                "+91 99497 96016",
                "CUSTOMER"
        );
        usersByEmail.put(defaultUser.getEmail().toLowerCase(), defaultUser);

        User adminUser = new User(
                UUID.randomUUID().toString(),
                "Administrator",
                "admin@bharatbazaar.in",
                "admin123",
                "+91 80 4567 8900",
                "ADMIN"
        );
        usersByEmail.put(adminUser.getEmail().toLowerCase(), adminUser);
    }

    /**
     * Authenticate user with email and password
     */
    public User authenticate(String email, String password) {
        if (email == null || password == null) return null;
        User user = usersByEmail.get(email.trim().toLowerCase());
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * Register a new user account
     */
    public User register(String name, String email, String password, String phone) throws IllegalArgumentException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Full Name is required.");
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new IllegalArgumentException("Valid Email Address is required.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (usersByEmail.containsKey(normalizedEmail)) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        User newUser = new User(
                UUID.randomUUID().toString(),
                name.trim(),
                normalizedEmail,
                password,
                phone != null ? phone.trim() : "",
                "CUSTOMER"
        );

        usersByEmail.put(normalizedEmail, newUser);

        // Asynchronously synchronize user to Firebase Realtime Database
        syncUserToFirebase(newUser);

        return newUser;
    }

    /**
     * Store user in Firebase Realtime Database under /users/{safeEmail}.json
     */
    private void syncUserToFirebase(User user) {
        if (!firebaseEnabled || databaseUrl == null || databaseUrl.isEmpty()) return;

        // Firebase keys cannot contain '.' so we replace it with '_'
        String safeKey = user.getEmail().replace(".", "_").replace("@", "_at_");
        String endpoint = databaseUrl + "/users/" + safeKey + ".json";

        String json = "{"
                + "\"id\":\"" + user.getId() + "\","
                + "\"name\":\"" + escapeJson(user.getName()) + "\","
                + "\"email\":\"" + escapeJson(user.getEmail()) + "\","
                + "\"phone\":\"" + escapeJson(user.getPhone()) + "\","
                + "\"role\":\"" + user.getRole() + "\""
                + "}";

        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(6))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    System.out.println("[Firebase] User profile synced to cloud: " + user.getEmail());
                }
            } catch (Exception e) {
                System.out.println("[Firebase] User profile cached locally: " + e.getMessage());
            }
        });
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
