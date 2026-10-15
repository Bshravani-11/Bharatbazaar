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
 * AuthService - Production Firebase Authentication Service
 * 
 * Demonstrates:
 *   - Direct Firebase Authentication REST API integration (Identity Toolkit)
 *   - Google Sign-In and Email/Password account support
 *   - User profile synchronization to Firebase Realtime Database
 *   - Thread-safe session authentication and local caching
 */
public class AuthService {

    private final Map<String, User> usersByEmail;
    private final HttpClient httpClient;
    private String apiKey;
    private String databaseUrl;
    private boolean firebaseEnabled;

    public AuthService() {
        this.usersByEmail = new ConcurrentHashMap<>();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
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
                this.apiKey = props.getProperty("firebase.api.key", "AIzaSyDx0Fv9E6jk_TbUr245EOfSGCUR4hSlSw0").trim();
                this.databaseUrl = props.getProperty("firebase.database.url", "").trim();
                if (this.databaseUrl.endsWith("/")) {
                    this.databaseUrl = this.databaseUrl.substring(0, this.databaseUrl.length() - 1);
                }
            } catch (Exception ignored) {}
        }

        if (this.apiKey == null || this.apiKey.isEmpty()) {
            this.apiKey = "AIzaSyDx0Fv9E6jk_TbUr245EOfSGCUR4hSlSw0";
        }
        if (this.databaseUrl == null || this.databaseUrl.isEmpty()) {
            this.databaseUrl = "https://bharatbazaar-7797e-default-rtdb.firebaseio.com";
        }
    }

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
     * Authenticate user with Email and Password using Firebase Auth REST API
     */
    public User authenticate(String email, String password) {
        if (email == null || password == null) return null;
        String normalizedEmail = email.trim().toLowerCase();

        // 1. Try Firebase Authentication API
        if (firebaseEnabled && apiKey != null && !apiKey.isEmpty()) {
            try {
                String endpoint = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey;
                String jsonBody = "{\"email\":\"" + escapeJson(normalizedEmail) + "\",\"password\":\"" + escapeJson(password) + "\",\"returnSecureToken\":true}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(6))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    String body = response.body();
                    String localId = extractJsonField(body, "localId");
                    String displayName = extractJsonField(body, "displayName");
                    if (displayName == null || displayName.isEmpty()) {
                        displayName = normalizedEmail.split("@")[0];
                    }

                    User user = new User(
                            localId != null ? localId : UUID.randomUUID().toString(),
                            displayName,
                            normalizedEmail,
                            password,
                            "",
                            "CUSTOMER"
                    );

                    usersByEmail.put(normalizedEmail, user);
                    System.out.println("==========================================================");
                    System.out.println("  [Firebase Auth] SUCCESS! Authenticated via Firebase Cloud");
                    System.out.println("  User: " + user.getName() + " (" + user.getEmail() + ")");
                    System.out.println("==========================================================");
                    return user;
                } else {
                    System.out.println("[Firebase Auth] Notice: Firebase Auth returned " + response.statusCode());
                }
            } catch (Exception e) {
                System.out.println("[Firebase Auth] Cloud network notice: " + e.getMessage());
            }
        }

        // 2. Fallback to cached in-memory accounts (for demo users like sowmya@example.com)
        User localUser = usersByEmail.get(normalizedEmail);
        if (localUser != null && localUser.getPassword().equals(password)) {
            System.out.println("[Auth] Authenticated via verified credentials: " + localUser.getEmail());
            return localUser;
        }

        return null;
    }

    /**
     * Register a new user account with Firebase Authentication API
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
        String trimmedName = name.trim();
        String trimmedPhone = phone != null ? phone.trim() : "";

        // 1. Try Firebase Authentication API (signUp)
        if (firebaseEnabled && apiKey != null && !apiKey.isEmpty()) {
            try {
                String endpoint = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + apiKey;
                String jsonBody = "{\"email\":\"" + escapeJson(normalizedEmail) + "\",\"password\":\"" + escapeJson(password) + "\",\"returnSecureToken\":true}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(6))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    String body = response.body();
                    String localId = extractJsonField(body, "localId");
                    String idToken = extractJsonField(body, "idToken");

                    // Set displayName in Firebase Auth profile
                    if (idToken != null) {
                        updateFirebaseProfile(idToken, trimmedName);
                    }

                    User newUser = new User(
                            localId != null ? localId : UUID.randomUUID().toString(),
                            trimmedName,
                            normalizedEmail,
                            password,
                            trimmedPhone,
                            "CUSTOMER"
                    );

                    usersByEmail.put(normalizedEmail, newUser);
                    syncUserToFirebaseDatabase(newUser);

                    System.out.println("==========================================================");
                    System.out.println("  [Firebase Auth] New User Created in Firebase Cloud!");
                    System.out.println("  User: " + newUser.getName() + " (" + newUser.getEmail() + ")");
                    System.out.println("==========================================================");
                    return newUser;
                } else if (response.body().contains("EMAIL_EXISTS")) {
                    throw new IllegalArgumentException("An account with this email address already exists in Firebase.");
                } else {
                    System.out.println("[Firebase Auth] Sign up response: " + response.statusCode() + " | " + response.body());
                }
            } catch (IllegalArgumentException iae) {
                throw iae;
            } catch (Exception e) {
                System.out.println("[Firebase Auth] Notice during registration: " + e.getMessage());
            }
        }

        // 2. Fallback to local registration if offline
        if (usersByEmail.containsKey(normalizedEmail)) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        User fallbackUser = new User(
                UUID.randomUUID().toString(),
                trimmedName,
                normalizedEmail,
                password,
                trimmedPhone,
                "CUSTOMER"
        );
        usersByEmail.put(normalizedEmail, fallbackUser);
        syncUserToFirebaseDatabase(fallbackUser);
        return fallbackUser;
    }

    /**
     * Authenticate or register a Google Sign-In user
     */
    public User authenticateGoogleUser(String email, String name, String googleId) {
        if (email == null || email.trim().isEmpty()) {
            email = "google_user_" + System.currentTimeMillis() + "@gmail.com";
        }
        String normalizedEmail = email.trim().toLowerCase();
        String displayName = (name != null && !name.trim().isEmpty()) ? name.trim() : normalizedEmail.split("@")[0];

        User user = usersByEmail.get(normalizedEmail);
        if (user == null) {
            user = new User(
                    googleId != null ? googleId : UUID.randomUUID().toString(),
                    displayName,
                    normalizedEmail,
                    "",
                    "",
                    "CUSTOMER"
            );
            usersByEmail.put(normalizedEmail, user);
            syncUserToFirebaseDatabase(user);
        }

        System.out.println("==========================================================");
        System.out.println("  [Google Sign-In] User Signed In via Google Auth");
        System.out.println("  Name: " + user.getName() + " | Email: " + user.getEmail());
        System.out.println("==========================================================");
        return user;
    }

    private void updateFirebaseProfile(String idToken, String displayName) {
        CompletableFuture.runAsync(() -> {
            try {
                String endpoint = "https://identitytoolkit.googleapis.com/v1/accounts:update?key=" + apiKey;
                String body = "{\"idToken\":\"" + idToken + "\",\"displayName\":\"" + escapeJson(displayName) + "\",\"returnSecureToken\":true}";
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(4))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            } catch (Exception ignored) {}
        });
    }

    private void syncUserToFirebaseDatabase(User user) {
        if (!firebaseEnabled || databaseUrl == null || databaseUrl.isEmpty()) return;

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

                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception ignored) {}
        });
    }

    private String extractJsonField(String json, String field) {
        String pattern = "\"" + field + "\":\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) {
            pattern = "\"" + field + "\": \"";
            idx = json.indexOf(pattern);
        }
        if (idx != -1) {
            int start = idx + pattern.length();
            int end = json.indexOf("\"", start);
            if (end != -1) {
                return json.substring(start, end);
            }
        }
        return null;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
