package com.shopmart.service;

import com.shopmart.model.CartItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * FirebaseService - Cloud Database Integration for Shopping Cart
 * 
 * B.Tech 2nd Year Assignment
 * 
 * Demonstrates:
 *   - Firebase Realtime Database REST API integration using standard Java HttpClient
 *   - Asynchronous HTTP networking with CompletableFuture (non-blocking)
 *   - Cloud persistence of customer orders, COD status, and cart data
 *   - Robust error handling with graceful offline fallback
 */
public class FirebaseService {

    public static class OrderRecord {
        public String orderNumber;
        public String customerName;
        public String phone;
        public String email;
        public String address;
        public String total;
        public String paymentMethod;
        public String timestamp;
        public String syncStatus;

        public OrderRecord(String orderNumber, String customerName, String phone, String email,
                           String address, String total, String paymentMethod, String timestamp, String syncStatus) {
            this.orderNumber = orderNumber;
            this.customerName = customerName;
            this.phone = phone;
            this.email = email;
            this.address = address;
            this.total = total;
            this.paymentMethod = paymentMethod;
            this.timestamp = timestamp;
            this.syncStatus = syncStatus;
        }
    }

    private String databaseUrl;
    private boolean enabled;
    private final HttpClient httpClient;
    private final List<OrderRecord> orderRecords; // thread-safe cached records

    public FirebaseService() {
        this.orderRecords = new ArrayList<>();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        loadConfiguration();
    }

    /**
     * Load Firebase configuration from firebase-config.properties file
     */
    private void loadConfiguration() {
        Properties props = new Properties();
        File configFile = new File("firebase-config.properties");

        if (configFile.exists()) {
            try (InputStream in = new FileInputStream(configFile)) {
                props.load(in);
                this.enabled = Boolean.parseBoolean(props.getProperty("firebase.enabled", "true"));
                this.databaseUrl = props.getProperty("firebase.database.url", "").trim();

                // Remove trailing slash if present
                if (this.databaseUrl.endsWith("/")) {
                    this.databaseUrl = this.databaseUrl.substring(0, this.databaseUrl.length() - 1);
                }

                System.out.println("==========================================================");
                System.out.println("  [Firebase] Cloud Integration Initialized");
                System.out.println("  [Firebase] Database URL: " + this.databaseUrl);
                System.out.println("  [Firebase] Status: " + (this.enabled ? "ENABLED" : "DISABLED"));
                System.out.println("==========================================================");
                return;
            } catch (Exception e) {
                System.out.println("[Firebase] Error loading config: " + e.getMessage());
            }
        }

        // Fallback defaults
        this.enabled = true;
        this.databaseUrl = "https://bharatbazaar-btech-default-rtdb.asia-southeast1.firebasedatabase.app";
    }

    /**
     * Save an order to Firebase Realtime Database asynchronously.
     * Uses the Firebase REST API: PUT /orders/{orderNumber}.json
     */
    public void saveOrder(String orderNumber, String customerName, String phone,
                          String email, String address, List<CartItem> items, double total) {

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        String jsonPayload = buildOrderJson(orderNumber, customerName, phone, email, address, items, total, timestamp);

        OrderRecord record = new OrderRecord(
                orderNumber, customerName, phone, email, address,
                String.format("%,.2f", total), "Cash on Delivery (COD)", timestamp, "Syncing..."
        );

        synchronized (orderRecords) {
            orderRecords.add(0, record); // newest first
        }

        if (!enabled || databaseUrl == null || databaseUrl.isEmpty()) {
            record.syncStatus = "Saved Locally";
            System.out.println("[Firebase] Cloud sync skipped (Firebase disabled in config)");
            return;
        }

        // Target Firebase endpoint: https://<database>.firebaseio.com/orders/<orderNumber>.json
        String endpoint = databaseUrl + "/orders/" + orderNumber + ".json";

        CompletableFuture.runAsync(() -> {
            try {
                System.out.println("[Firebase] Syncing order " + orderNumber + " to Firebase Realtime Database...");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(8))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    record.syncStatus = "Synced to Firebase (200 OK)";
                    System.out.println("==========================================================");
                    System.out.println("  [Firebase] SUCCESS! Order " + orderNumber + " stored in Cloud");
                    System.out.println("  [Firebase] Status: " + response.statusCode() + " OK");
                    System.out.println("==========================================================");
                } else {
                    record.syncStatus = "Firebase Status " + response.statusCode();
                    System.out.println("[Firebase] Server responded with code: " + response.statusCode() + " | " + response.body());
                }
            } catch (Exception e) {
                record.syncStatus = "Saved Locally (Offline)";
                System.out.println("[Firebase] Notice: " + e.getMessage() + " (Order saved in local database)");
            }
        });
    }

    /**
     * Get list of all orders
     */
    public List<OrderRecord> getOrderRecords() {
        synchronized (orderRecords) {
            return new ArrayList<>(orderRecords);
        }
    }

    /**
     * Build clean JSON string for Firebase
     */
    private String buildOrderJson(String orderNumber, String customerName, String phone,
                                  String email, String address, List<CartItem> items,
                                  double total, String timestamp) {

        StringBuilder sb = new StringBuilder();
        sb.append("{")
          .append("\"orderNumber\":").append(escapeJson(orderNumber)).append(",")
          .append("\"customerName\":").append(escapeJson(customerName)).append(",")
          .append("\"phone\":").append(escapeJson(phone)).append(",")
          .append("\"email\":").append(escapeJson(email)).append(",")
          .append("\"address\":").append(escapeJson(address)).append(",")
          .append("\"paymentMethod\":\"Cash on Delivery (COD)\",")
          .append("\"paymentStatus\":\"Pending (Pay in Cash upon Delivery)\",")
          .append("\"total\":").append(String.format("%.2f", total)).append(",")
          .append("\"timestamp\":").append(escapeJson(timestamp)).append(",")
          .append("\"items\":[");

        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            sb.append("{")
              .append("\"productName\":").append(escapeJson(item.getProduct().getName())).append(",")
              .append("\"quantity\":").append(item.getQuantity()).append(",")
              .append("\"subtotal\":").append(String.format("%.2f", item.getSubtotal()))
              .append("}");
            if (i < items.size() - 1) sb.append(",");
        }

        sb.append("]}");
        return sb.toString();
    }

    private String escapeJson(String str) {
        if (str == null) return "\"\"";
        return "\"" + str.replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\b", "\\b")
                        .replace("\f", "\\f")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t") + "\"";
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
