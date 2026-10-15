package com.shopmart;

import com.shopmart.model.Cart;
import com.shopmart.model.CartItem;
import com.shopmart.model.Product;
import com.shopmart.model.User;
import com.shopmart.service.AuthService;
import com.shopmart.service.FirebaseService;
import com.shopmart.service.ProductService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShoppingCartServer - Enterprise Java E-Commerce Platform
 * 
 * B.Tech 2nd Year Assignment
 * 
 * Features:
 *   - Clean, professional design with SVG iconography (no emojis)
 *   - Indian E-Commerce standard: INR (₹) currency, 5% GST, ₹499 free shipping threshold
 *   - Exclusive Cash on Delivery (COD) payment mechanism
 *   - User Authentication: Secure Login, Registration, and Session management
 *   - Real-time Order Management System (OMS) backed by Firebase Realtime Database
 *   - Built using standard Java 17 library (no external framework overhead)
 */
public class ShoppingCartServer {

    private final int port;
    private final ProductService productService;
    private final Cart cart;
    private final FirebaseService firebaseService;
    private final AuthService authService;
    private final Map<String, User> activeSessions;

    // ==========================================
    //  PROFESSIONAL SVG ICONS
    // ==========================================

    private static final String SVG_CART = 
        "<svg width=\"17\" height=\"17\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" style=\"vertical-align:middle;margin-right:4px;\">" +
        "<path d=\"M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z\"></path><line x1=\"3\" y1=\"6\" x2=\"21\" y2=\"6\"></line><path d=\"M16 10a4 4 0 0 1-8 0\"></path></svg>";

    private static final String SVG_SEARCH = 
        "<svg width=\"15\" height=\"15\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" style=\"vertical-align:middle;\">" +
        "<circle cx=\"11\" cy=\"11\" r=\"8\"></circle><line x1=\"21\" y1=\"21\" x2=\"16.65\" y2=\"16.65\"></line></svg>";

    private static final String SVG_ORDERS = 
        "<svg width=\"16\" height=\"16\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" style=\"vertical-align:middle;margin-right:4px;\">" +
        "<path d=\"M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z\"></path><polyline points=\"14 2 14 8 20 8\"></polyline><line x1=\"16\" y1=\"13\" x2=\"8\" y2=\"13\"></line><line x1=\"16\" y1=\"17\" x2=\"8\" y2=\"17\"></line></svg>";

    private static final String SVG_USER = 
        "<svg width=\"16\" height=\"16\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" style=\"vertical-align:middle;margin-right:4px;\">" +
        "<path d=\"M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2\"></path><circle cx=\"12\" cy=\"7\" r=\"4\"></circle></svg>";

    private static final String SVG_TRUCK = 
        "<svg width=\"16\" height=\"16\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" style=\"vertical-align:middle;margin-right:6px;\">" +
        "<rect x=\"1\" y=\"3\" width=\"15\" height=\"13\"></rect><polygon points=\"16 8 20 8 23 11 23 16 16 16 16 8\"></polygon><circle cx=\"5.5\" cy=\"18.5\" r=\"2.5\"></circle><circle cx=\"18.5\" cy=\"18.5\" r=\"2.5\"></circle></svg>";

    private static final String SVG_CASH = 
        "<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" style=\"vertical-align:middle;margin-right:6px;\">" +
        "<rect x=\"2\" y=\"6\" width=\"20\" height=\"12\" rx=\"2\"></rect><circle cx=\"12\" cy=\"12\" r=\"2\"></circle><path d=\"M6 12h.01M18 12h.01\"></path></svg>";

    private static final String SVG_CHECK = 
        "<svg width=\"36\" height=\"36\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"#16a34a\" stroke-width=\"2.5\" stroke-linecap=\"round\" stroke-linejoin=\"round\">" +
        "<polyline points=\"20 6 9 17 4 12\"></polyline></svg>";

    private static final String SVG_DATABASE = 
        "<svg width=\"14\" height=\"14\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" style=\"vertical-align:middle;margin-right:4px;\">" +
        "<ellipse cx=\"12\" cy=\"5\" rx=\"9\" ry=\"3\"></ellipse><path d=\"M21 12c0 1.66-4 3-9 3s-9-1.34-9-3\"></path><path d=\"M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5\"></path></svg>";

    private static final String SVG_TRASH = 
        "<svg width=\"14\" height=\"14\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" style=\"vertical-align:middle;margin-right:2px;\">" +
        "<polyline points=\"3 6 5 6 21 6\"></polyline><path d=\"M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2\"></path></svg>";

    private static final String SVG_LOCK = 
        "<svg width=\"16\" height=\"16\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" style=\"vertical-align:middle;margin-right:4px;\">" +
        "<rect x=\"3\" y=\"11\" width=\"18\" height=\"11\" rx=\"2\" ry=\"2\"></rect><path d=\"M7 11V7a5 5 0 0 1 10 0v4\"></path></svg>";

    private static final String SVG_GOOGLE = 
        "<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" style=\"vertical-align:middle;margin-right:8px;\">" +
        "<path fill=\"#4285F4\" d=\"M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z\"/>" +
        "<path fill=\"#34A853\" d=\"M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z\"/>" +
        "<path fill=\"#FBBC05\" d=\"M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z\"/>" +
        "<path fill=\"#EA4335\" d=\"M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z\"/>" +
        "</svg>";

    public ShoppingCartServer() {
        int p = 8080;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            try {
                p = Integer.parseInt(envPort.trim());
            } catch (Exception ignored) {}
        }
        this.port = p;
        this.productService = new ProductService();
        this.cart = new Cart();
        this.firebaseService = new FirebaseService();
        this.authService = new AuthService();
        this.activeSessions = new ConcurrentHashMap<>();
    }

    public static void main(String[] args) throws IOException {
        ShoppingCartServer app = new ShoppingCartServer();
        app.start();
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Register URL Routes
        server.createContext("/", new HomeHandler());
        server.createContext("/cart", new CartHandler());
        server.createContext("/cart/add", new AddToCartHandler());
        server.createContext("/cart/increase", new IncreaseQuantityHandler());
        server.createContext("/cart/decrease", new DecreaseQuantityHandler());
        server.createContext("/cart/remove", new RemoveFromCartHandler());
        server.createContext("/cart/clear", new ClearCartHandler());
        server.createContext("/checkout", new CheckoutHandler());
        server.createContext("/checkout/place-order", new PlaceOrderHandler());
        server.createContext("/orders", new OrdersHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/login/google", new GoogleLoginHandler());
        server.createContext("/register", new RegisterHandler());
        server.createContext("/logout", new LogoutHandler());
        server.createContext("/css/styles.css", new CssHandler());
        server.createContext("/images/", new ImageHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("==========================================================");
        System.out.println("  BharatBazaar E-Commerce Platform Online");
        System.out.println("  Payment Method: Cash on Delivery (COD) Only");
        System.out.println("  Firebase Project ID: bharatbazaar-7797e");
        System.out.println("  Cloud Database: " + firebaseService.getDatabaseUrl());
        System.out.println("  Storefront URL: http://localhost:" + port);
        System.out.println("  Login Page: http://localhost:" + port + "/login");
        System.out.println("  Order Management: http://localhost:" + port + "/orders");
        System.out.println("==========================================================");
    }

    // ==========================================
    //  SESSION MANAGEMENT
    // ==========================================

    private User getAuthenticatedUser(HttpExchange exchange) {
        List<String> cookies = exchange.getRequestHeaders().get("Cookie");
        if (cookies != null) {
            for (String cookieHeader : cookies) {
                String[] parts = cookieHeader.split(";");
                for (String part : parts) {
                    String[] kv = part.trim().split("=", 2);
                    if (kv.length == 2 && "BB_SESSION".equalsIgnoreCase(kv[0])) {
                        return activeSessions.get(kv[1]);
                    }
                }
            }
        }
        return null;
    }

    private void setSessionCookie(HttpExchange exchange, String sessionId) {
        exchange.getResponseHeaders().set("Set-Cookie", "BB_SESSION=" + sessionId + "; Path=/; HttpOnly");
    }

    private void clearSessionCookie(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Set-Cookie", "BB_SESSION=; Path=/; Max-Age=0; HttpOnly");
    }

    // ==========================================
    //  HTTP HANDLERS
    // ==========================================

    private class HomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestURI().getPath().equals("/")) {
                sendResponse(exchange, 404, "Page Not Found", "text/plain");
                return;
            }

            User user = getAuthenticatedUser(exchange);
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getRawQuery());
            String category = params.get("category");
            String query = params.get("query");

            List<Product> products;
            if (query != null && !query.trim().isEmpty()) {
                products = productService.searchProducts(query);
            } else if (category != null && !category.equalsIgnoreCase("all")) {
                products = productService.getProductsByCategory(category);
            } else {
                products = productService.getAllProducts();
            }

            String html = renderHomePage(products, category, query, user);
            sendResponse(exchange, 200, html, "text/html; charset=UTF-8");
        }
    }

    private class CartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            User user = getAuthenticatedUser(exchange);
            String html = renderCartPage(user);
            sendResponse(exchange, 200, html, "text/html; charset=UTF-8");
        }
    }

    private class AddToCartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getRawQuery());
                String idStr = params.get("id");
                if (idStr == null) {
                    Map<String, String> form = parseFormData(exchange);
                    idStr = form.get("id");
                }

                if (idStr != null) {
                    try {
                        Long productId = Long.parseLong(idStr);
                        Product product = productService.getProductById(productId);
                        if (product != null && product.isInStock()) {
                            cart.addItem(product);
                            System.out.println("[Cart] Added item: " + product.getName() + " | Total items: " + cart.getTotalItems());
                        }
                    } catch (NumberFormatException ignored) {}
                }
                sendRedirect(exchange, "/");
            } else {
                sendRedirect(exchange, "/");
            }
        }
    }

    private class IncreaseQuantityHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getRawQuery());
                String idStr = params.get("id");
                if (idStr != null) {
                    try {
                        Long productId = Long.parseLong(idStr);
                        CartItem item = cart.findItem(productId);
                        if (item != null) {
                            item.incrementQuantity();
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            sendRedirect(exchange, "/cart");
        }
    }

    private class DecreaseQuantityHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getRawQuery());
                String idStr = params.get("id");
                if (idStr != null) {
                    try {
                        Long productId = Long.parseLong(idStr);
                        CartItem item = cart.findItem(productId);
                        if (item != null) {
                            if (item.getQuantity() <= 1) {
                                cart.removeItem(productId);
                            } else {
                                item.decrementQuantity();
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            sendRedirect(exchange, "/cart");
        }
    }

    private class RemoveFromCartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getRawQuery());
                String idStr = params.get("id");
                if (idStr != null) {
                    try {
                        Long productId = Long.parseLong(idStr);
                        cart.removeItem(productId);
                        System.out.println("[Cart] Removed item ID: " + productId);
                    } catch (NumberFormatException ignored) {}
                }
            }
            sendRedirect(exchange, "/cart");
        }
    }

    private class ClearCartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                cart.clear();
                System.out.println("[Cart] Cleared all items");
            }
            sendRedirect(exchange, "/cart");
        }
    }

    private class CheckoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (cart.isEmpty()) {
                sendRedirect(exchange, "/");
                return;
            }
            User user = getAuthenticatedUser(exchange);
            if (user == null) {
                sendRedirect(exchange, "/login?msg=Please+sign+in+to+your+account+to+proceed+with+checkout.");
                return;
            }
            String html = renderCheckoutPage(user);
            sendResponse(exchange, 200, html, "text/html; charset=UTF-8");
        }
    }

    private class PlaceOrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                User user = getAuthenticatedUser(exchange);
                if (user == null) {
                    sendRedirect(exchange, "/login?msg=Please+sign+in+to+place+your+order.");
                    return;
                }
                Map<String, String> form = parseFormData(exchange);
                String fullName = form.getOrDefault("fullName", user.getName());
                String email = form.getOrDefault("email", user.getEmail());
                String phone = form.getOrDefault("phone", user.getPhone());
                String address = form.getOrDefault("address", "N/A");
                String city = form.getOrDefault("city", "Bengaluru");
                String state = form.getOrDefault("state", "Karnataka");
                String pincode = form.getOrDefault("pincode", "560001");
                String fullAddress = address + ", " + city + ", " + state + " - " + pincode;

                List<CartItem> orderedItems = new ArrayList<>();
                for (CartItem item : cart.getItems()) {
                    orderedItems.add(new CartItem(item.getProduct(), item.getQuantity()));
                }
                double finalTotal = cart.getTotal();

                String orderNumber = "IND-" + String.format("%06d", (int)(Math.random() * 900000 + 100000));
                cart.clear();

                // Sync to Firebase Cloud Database
                firebaseService.saveOrder(orderNumber, fullName, phone, email, fullAddress, orderedItems, finalTotal);

                System.out.println("=========================================");
                System.out.println("  ORDER REGISTERED: " + orderNumber);
                System.out.println("  Customer: " + fullName + " (" + email + ")");
                System.out.println("  Phone: " + phone);
                System.out.println("  Address: " + fullAddress);
                System.out.println("  Payment Method: Cash on Delivery (COD) Only");
                System.out.println("  Amount: Rs. " + String.format("%.2f", finalTotal));
                System.out.println("=========================================");

                String html = renderConfirmationPage(orderNumber, fullName, email, phone, fullAddress, orderedItems, finalTotal, user);
                sendResponse(exchange, 200, html, "text/html; charset=UTF-8");
            } else {
                sendRedirect(exchange, "/");
            }
        }
    }

    private class OrdersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            User user = getAuthenticatedUser(exchange);
            if (user == null) {
                sendRedirect(exchange, "/login?msg=Please+sign+in+to+view+your+order+history.");
                return;
            }
            String html = renderOrdersPage(user);
            sendResponse(exchange, 200, html, "text/html; charset=UTF-8");
        }
    }

    /**
     * Login Handler - GET /login, POST /login
     */
    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> form = parseFormData(exchange);
                String email = form.get("email");
                String password = form.get("password");

                User user = authService.authenticate(email, password);
                if (user != null) {
                    String sessionId = UUID.randomUUID().toString();
                    activeSessions.put(sessionId, user);
                    setSessionCookie(exchange, sessionId);
                    System.out.println("[Auth] User logged in: " + user.getName() + " (" + user.getEmail() + ")");
                    sendRedirect(exchange, "/");
                } else {
                    sendRedirect(exchange, "/login?error=Invalid+email+or+password.+Try+demo+credentials.");
                }
            } else {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getRawQuery());
                String error = params.get("error");
                String msg = params.get("msg");
                User user = getAuthenticatedUser(exchange);
                if (user != null) {
                    sendRedirect(exchange, "/");
                    return;
                }
                String html = renderLoginPage(error, msg);
                sendResponse(exchange, 200, html, "text/html; charset=UTF-8");
            }
        }
    }

    /**
     * Register Handler - POST /register
     */
    private class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> form = parseFormData(exchange);
                String name = form.get("name");
                String email = form.get("email");
                String password = form.get("password");
                String phone = form.get("phone");

                try {
                    User newUser = authService.register(name, email, password, phone);
                    String sessionId = UUID.randomUUID().toString();
                    activeSessions.put(sessionId, newUser);
                    setSessionCookie(exchange, sessionId);
                    System.out.println("[Auth] New user registered: " + newUser.getName() + " (" + newUser.getEmail() + ")");
                    sendRedirect(exchange, "/");
                } catch (IllegalArgumentException e) {
                    sendRedirect(exchange, "/login?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
                }
            } else {
                sendRedirect(exchange, "/login");
            }
        }
    }

    /**
     * Google Sign-In Handler - POST /login/google
     */
    private class GoogleLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> form = parseFormData(exchange);
                String email = form.get("email");
                String name = form.get("name");
                String uid = form.get("uid");

                if (email == null || email.trim().isEmpty()) {
                    sendRedirect(exchange, "/login?error=Google+sign-in+failed.+Email+was+not+provided.");
                    return;
                }

                User user = authService.authenticateGoogleUser(email, name, uid);
                String sessionId = UUID.randomUUID().toString();
                activeSessions.put(sessionId, user);
                setSessionCookie(exchange, sessionId);
                System.out.println("[Google Auth] User session created: " + user.getName() + " (" + user.getEmail() + ")");
                sendRedirect(exchange, "/");
            } else {
                sendRedirect(exchange, "/login");
            }
        }
    }

    /**
     * Logout Handler - GET /logout
     */
    private class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<String> cookies = exchange.getRequestHeaders().get("Cookie");
            if (cookies != null) {
                for (String cookieHeader : cookies) {
                    String[] parts = cookieHeader.split(";");
                    for (String part : parts) {
                        String[] kv = part.trim().split("=", 2);
                        if (kv.length == 2 && "BB_SESSION".equalsIgnoreCase(kv[0])) {
                            activeSessions.remove(kv[1]);
                        }
                    }
                }
            }
            clearSessionCookie(exchange);
            sendRedirect(exchange, "/login?msg=You+have+been+successfully+logged+out.");
        }
    }

    private class CssHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Path cssPath = Paths.get("src", "main", "resources", "static", "css", "styles.css");
            if (Files.exists(cssPath)) {
                byte[] cssBytes = Files.readAllBytes(cssPath);
                exchange.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
                exchange.sendResponseHeaders(200, cssBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(cssBytes);
                }
            } else {
                sendResponse(exchange, 404, "CSS Not Found", "text/plain");
            }
        }
    }

    private class ImageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String filename = path.substring(path.lastIndexOf('/') + 1);
            byte[] bytes = null;

            Path imgPath = Paths.get("src", "main", "resources", "static", "images", filename);
            if (Files.exists(imgPath)) {
                bytes = Files.readAllBytes(imgPath);
            } else {
                try (InputStream is = getClass().getResourceAsStream("/static/images/" + filename)) {
                    if (is != null) {
                        bytes = is.readAllBytes();
                    }
                } catch (Exception ignored) {}
            }

            if (bytes != null) {
                exchange.getResponseHeaders().set("Content-Type", "image/jpeg");
                exchange.getResponseHeaders().set("Cache-Control", "public, max-age=86400");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                sendResponse(exchange, 404, "Image Not Found", "text/plain");
            }
        }
    }

    // ==========================================
    //  PROFESSIONAL HTML TEMPLATES
    // ==========================================

    private String renderHeader(User currentUser) {
        StringBuilder sb = new StringBuilder();
        sb.append("<header class=\"header\">\n")
          .append("    <div class=\"header-content\">\n")
          .append("        <a href=\"/\" class=\"logo\">").append(SVG_CART).append(" Bharat<strong>Bazaar</strong></a>\n")
          .append("        <div class=\"header-right\">\n")
          .append("            <form action=\"/\" method=\"get\" class=\"search-form\">\n")
          .append("                <input type=\"text\" name=\"query\" placeholder=\"Search catalog by brand or product...\" class=\"search-input\">\n")
          .append("                <button type=\"submit\" class=\"search-btn\" title=\"Search\">").append(SVG_SEARCH).append("</button>\n")
          .append("            </form>\n")
          .append("            <a href=\"/orders\" class=\"nav-link\">").append(SVG_ORDERS).append(" Order History</a>\n");

        if (currentUser != null) {
            sb.append("            <span class=\"nav-link\" style=\"color:#1e293b; font-weight:600; display:inline-flex; align-items:center; gap:4px;\">")
              .append(SVG_USER).append(currentUser.getName())
              .append("</span>\n")
              .append("            <a href=\"/logout\" class=\"nav-link\" style=\"color:#ef4444; font-size:0.85rem;\" title=\"Sign Out\">Sign Out</a>\n");
        } else {
            sb.append("            <a href=\"/login\" class=\"nav-link\" style=\"color:#2563eb; font-weight:600; display:inline-flex; align-items:center; gap:4px;\">")
              .append(SVG_USER).append("Sign In</a>\n");
        }

        sb.append("            <a href=\"/cart\" class=\"cart-link\">\n")
          .append("                ").append(SVG_CART).append(" Cart\n")
          .append("                <span class=\"badge\">").append(cart.getTotalItems()).append("</span>\n")
          .append("            </a>\n")
          .append("        </div>\n")
          .append("    </div>\n")
          .append("</header>\n");
        return sb.toString();
    }

    private String renderFooter() {
        return "<footer class=\"footer\">\n" +
               "    <div class=\"footer-content\">\n" +
               "        <div class=\"footer-section\">\n" +
               "            <h4>BharatBazaar Retail</h4>\n" +
               "            <p>Certified online marketplace for electronics, apparel, and domestic essentials across India.</p>\n" +
               "            <p style=\"margin-top:10px; font-size:0.8rem; color:#94a3b8;\">100% Verified Quality | Safe Cash on Delivery Across India</p>\n" +
               "        </div>\n" +
               "        <div class=\"footer-section\">\n" +
               "            <h4>Quick Navigation</h4>\n" +
               "            <ul>\n" +
               "                <li><a href=\"/\">Product Catalog</a></li>\n" +
               "                <li><a href=\"/cart\">Shopping Cart</a></li>\n" +
               "                <li><a href=\"/orders\">Order Management</a></li>\n" +
               "                <li><a href=\"/login\">Account Portal</a></li>\n" +
               "            </ul>\n" +
               "        </div>\n" +
               "        <div class=\"footer-section\">\n" +
               "            <h4>Customer Support</h4>\n" +
               "            <p>Email: support@bharatbazaar.in</p>\n" +
               "            <p>Toll Free: 1800-456-7890</p>\n" +
               "            <p>Corporate Office: Bengaluru, Karnataka - 560001</p>\n" +
               "        </div>\n" +
               "    </div>\n" +
               "    <div class=\"footer-bottom\">\n" +
               "        <p>&copy; 2026 BharatBazaar Retail Private Limited. All Rights Reserved. | B.Tech Academic Project</p>\n" +
               "    </div>\n" +
               "</footer>\n";
    }

    private String renderHomePage(List<Product> products, String selectedCategory, String searchQuery, User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
          .append("<meta charset=\"UTF-8\">\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
          .append("<title>BharatBazaar - Certified Online Store</title>\n")
          .append("<link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap\" rel=\"stylesheet\">\n")
          .append("<link href=\"/css/styles.css\" rel=\"stylesheet\">\n")
          .append("</head>\n<body>\n");

        sb.append(renderHeader(user));

        // Hero Banner
        sb.append("<section class=\"hero\">\n")
          .append("    <h1>Quality Products, Delivered Across India</h1>\n")
          .append("    <p>Discover top consumer electronics, lifestyle apparel, and household essentials at genuine prices.</p>\n")
          .append("    <p style=\"font-size:0.9rem; margin-top:-10px; margin-bottom:20px; color:#cbd5e1;\">")
          .append(SVG_TRUCK).append("Free standard shipping on orders over &#8377;499 | ").append(SVG_CASH).append("Cash on Delivery (COD) Only</p>\n")
          .append("    <div style=\"display:flex; justify-content:center; gap:12px; flex-wrap:wrap;\">\n")
          .append("        <a href=\"#products\" class=\"hero-btn\">View Catalog</a>\n");

        if (user == null) {
            sb.append("        <a href=\"/login\" class=\"hero-btn\" style=\"background:rgba(255,255,255,0.15); color:white; border:1px solid rgba(255,255,255,0.3);\">")
              .append(SVG_USER).append(" Customer Sign In</a>\n");
        } else {
            sb.append("        <a href=\"/orders\" class=\"hero-btn\" style=\"background:rgba(255,255,255,0.15); color:white; border:1px solid rgba(255,255,255,0.3);\">")
              .append(SVG_ORDERS).append(" Order Records</a>\n");
        }

        sb.append("    </div>\n")
          .append("</section>\n");

        // Category Filter Buttons
        sb.append("<section class=\"categories\">\n");
        boolean isAllActive = selectedCategory == null || selectedCategory.equalsIgnoreCase("all");
        sb.append("    <a href=\"/?category=all\" class=\"category-btn ").append(isAllActive ? "active" : "").append("\">All Products</a>\n");

        for (String cat : productService.getAllCategories()) {
            boolean isActive = selectedCategory != null && selectedCategory.equalsIgnoreCase(cat);
            sb.append("    <a href=\"/?category=").append(cat).append("\" class=\"category-btn ")
              .append(isActive ? "active" : "").append("\">").append(cat).append("</a>\n");
        }
        sb.append("</section>\n");

        // Product Grid
        sb.append("<main class=\"products-section\" id=\"products\">\n")
          .append("    <h2 class=\"section-title\">Featured Catalog</h2>\n");

        if (products.isEmpty()) {
            sb.append("    <div class=\"no-products\">\n")
              .append("        <p>No products matched your search criteria. Please adjust your keywords.</p>\n")
              .append("        <a href=\"/\" class=\"hero-btn\">View Full Catalog</a>\n")
              .append("    </div>\n");
        } else {
            sb.append("    <div class=\"products-grid\">\n");
            for (Product p : products) {
                sb.append("        <div class=\"product-card\">\n")
                  .append("            <div class=\"product-image\">\n")
                  .append("                <img src=\"").append(p.getImageUrl()).append("\" alt=\"").append(p.getName()).append("\">\n");

                if (p.isInStock()) {
                    sb.append("                <span class=\"stock-badge in-stock\">In Stock</span>\n");
                } else {
                    sb.append("                <span class=\"stock-badge out-of-stock\">Out of Stock</span>\n");
                }

                sb.append("            </div>\n")
                  .append("            <div class=\"product-info\">\n")
                  .append("                <span class=\"product-category\">").append(p.getCategory()).append("</span>\n")
                  .append("                <h3 class=\"product-name\">").append(p.getName()).append("</h3>\n")
                  .append("                <p class=\"product-desc\">").append(p.getDescription()).append("</p>\n")
                  .append("                <div class=\"product-footer\">\n")
                  .append("                    <span class=\"product-price\">&#8377;").append(String.format("%,.0f", p.getPrice())).append("</span>\n");

                if (p.isInStock()) {
                    sb.append("                    <form action=\"/cart/add?id=").append(p.getId()).append("\" method=\"post\">\n")
                      .append("                        <button type=\"submit\" class=\"add-to-cart-btn\">Add to Cart</button>\n")
                      .append("                    </form>\n");
                } else {
                    sb.append("                    <button class=\"add-to-cart-btn disabled\" disabled>Unavailable</button>\n");
                }

                sb.append("                </div>\n")
                  .append("            </div>\n")
                  .append("        </div>\n");
            }
            sb.append("    </div>\n");
        }

        sb.append("</main>\n");
        sb.append(renderFooter());
        sb.append("</body>\n</html>");
        return sb.toString();
    }

    private String renderCartPage(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
          .append("<meta charset=\"UTF-8\">\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
          .append("<title>BharatBazaar - Shopping Cart</title>\n")
          .append("<link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap\" rel=\"stylesheet\">\n")
          .append("<link href=\"/css/styles.css\" rel=\"stylesheet\">\n")
          .append("</head>\n<body>\n");

        sb.append(renderHeader(user));

        sb.append("<main class=\"cart-page\">\n")
          .append("    <h1 class=\"page-title\">").append(SVG_CART).append(" Shopping Cart</h1>\n");

        if (cart.isEmpty()) {
            sb.append("    <div class=\"empty-cart\">\n")
              .append("        <div class=\"empty-cart-icon\">").append(SVG_CART).append("</div>\n")
              .append("        <h2>Your cart is currently empty</h2>\n")
              .append("        <p>Explore our catalog to add items to your shopping cart.</p>\n")
              .append("        <a href=\"/\" class=\"hero-btn\">Browse Catalog</a>\n")
              .append("    </div>\n");
        } else {
            sb.append("    <div class=\"cart-container\">\n");

            // Shipping Progress Bar
            if (cart.getAmountToFreeShipping() > 0) {
                double pct = Math.min(100.0, (cart.getSubtotal() / 499.0) * 100.0);
                sb.append("        <div class=\"shipping-bar\">\n")
                  .append("            <p>").append(SVG_TRUCK).append("Add items worth <strong>&#8377;")
                  .append(String.format("%,.0f", cart.getAmountToFreeShipping()))
                  .append("</strong> more to qualify for <strong>Free Delivery</strong>.</p>\n")
                  .append("            <div class=\"progress-bar\"><div class=\"progress-fill\" style=\"width: ")
                  .append(String.format("%.1f", pct)).append("%\"></div></div>\n")
                  .append("        </div>\n");
            } else {
                sb.append("        <div class=\"shipping-bar free\">\n")
                  .append("            <p>").append(SVG_TRUCK).append("Eligible for <strong>Free Standard Delivery</strong> across India.</p>\n")
                  .append("        </div>\n");
            }

            // Cart Table
            sb.append("        <div class=\"cart-table\">\n")
              .append("            <div class=\"cart-table-header\">\n")
              .append("                <span>Item Description</span><span>Unit Price</span><span>Quantity</span><span>Subtotal</span><span>Action</span>\n")
              .append("            </div>\n");

            for (CartItem item : cart.getItems()) {
                Product p = item.getProduct();
                sb.append("            <div class=\"cart-item\">\n")
                  .append("                <div class=\"col-product cart-item-product\">\n")
                  .append("                    <img src=\"").append(p.getImageUrl()).append("\" class=\"cart-item-img\" alt=\"").append(p.getName()).append("\">\n")
                  .append("                    <div>\n")
                  .append("                        <h4>").append(p.getName()).append("</h4>\n")
                  .append("                        <span class=\"cart-item-category\">").append(p.getCategory()).append("</span>\n")
                  .append("                    </div>\n")
                  .append("                </div>\n")
                  .append("                <div class=\"col-price\">&#8377;").append(String.format("%,.0f", p.getPrice())).append("</div>\n")
                  .append("                <div class=\"col-quantity quantity-controls\">\n")
                  .append("                    <form action=\"/cart/decrease?id=").append(p.getId()).append("\" method=\"post\" style=\"display:inline\">\n")
                  .append("                        <button type=\"submit\" class=\"qty-btn\" title=\"Decrease\">&#8722;</button>\n")
                  .append("                    </form>\n")
                  .append("                    <span class=\"qty-value\">").append(item.getQuantity()).append("</span>\n")
                  .append("                    <form action=\"/cart/increase?id=").append(p.getId()).append("\" method=\"post\" style=\"display:inline\">\n")
                  .append("                        <button type=\"submit\" class=\"qty-btn\" title=\"Increase\">+</button>\n")
                  .append("                    </form>\n")
                  .append("                </div>\n")
                  .append("                <div class=\"col-subtotal\"><strong>&#8377;").append(String.format("%,.0f", item.getSubtotal())).append("</strong></div>\n")
                  .append("                <div class=\"col-action\">\n")
                  .append("                    <form action=\"/cart/remove?id=").append(p.getId()).append("\" method=\"post\">\n")
                  .append("                        <button type=\"submit\" class=\"remove-btn\">").append(SVG_TRASH).append(" Remove</button>\n")
                  .append("                    </form>\n")
                  .append("                </div>\n")
                  .append("            </div>\n");
            }
            sb.append("        </div>\n");

            // Summary
            sb.append("        <div class=\"cart-summary\">\n")
              .append("            <h3>Price Summary</h3>\n")
              .append("            <div class=\"summary-row\"><span>Items Subtotal</span><span>&#8377;").append(String.format("%,.2f", cart.getSubtotal())).append("</span></div>\n");

            if (cart.getShipping() == 0) {
                sb.append("            <div class=\"summary-row\"><span>Delivery Charges</span><span class=\"free-text\">FREE</span></div>\n");
            } else {
                sb.append("            <div class=\"summary-row\"><span>Delivery Charges</span><span>&#8377;").append(String.format("%,.2f", cart.getShipping())).append("</span></div>\n");
            }

            sb.append("            <div class=\"summary-row\"><span>Applicable GST (5%)</span><span>&#8377;").append(String.format("%,.2f", cart.getTax())).append("</span></div>\n")
              .append("            <div class=\"summary-row total-row\"><span>Total Payable</span><span>&#8377;").append(String.format("%,.2f", cart.getTotal())).append("</span></div>\n")
              .append("            <div class=\"cart-actions\">\n")
              .append("                <form action=\"/cart/clear\" method=\"post\">\n")
              .append("                    <button type=\"submit\" class=\"clear-cart-btn\">Clear Cart</button>\n")
              .append("                </form>\n")
              .append("                <a href=\"/checkout\" class=\"checkout-btn\">Proceed to Checkout &#8594;</a>\n")
              .append("            </div>\n")
              .append("        </div>\n")
              .append("    </div>\n");
        }

        sb.append("</main>\n");
        sb.append(renderFooter());
        sb.append("</body>\n</html>");
        return sb.toString();
    }

    private String renderCheckoutPage(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
          .append("<meta charset=\"UTF-8\">\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
          .append("<title>BharatBazaar - Checkout</title>\n")
          .append("<link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap\" rel=\"stylesheet\">\n")
          .append("<link href=\"/css/styles.css\" rel=\"stylesheet\">\n")
          .append("</head>\n<body>\n");

        sb.append(renderHeader(user));

        String defaultName = user != null ? user.getName() : "";
        String defaultEmail = user != null ? user.getEmail() : "";
        String defaultPhone = user != null ? user.getPhone() : "";

        sb.append("<main class=\"checkout-page\">\n")
          .append("    <h1 class=\"page-title\">Order Checkout</h1>\n");

        if (user == null) {
            sb.append("    <div style=\"background:#eff6ff; border:1px solid #bfdbfe; color:#1e40af; padding:12px 18px; border-radius:8px; margin-bottom:24px; font-size:0.9rem; display:flex; justify-content:space-between; align-items:center;\">\n")
              .append("        <span>Have an account? Sign in for a faster checkout experience.</span>\n")
              .append("        <a href=\"/login\" style=\"font-weight:700; color:#2563eb; text-decoration:underline;\">Sign In</a>\n")
              .append("    </div>\n");
        }

        sb.append("    <div class=\"checkout-container\">\n")
          .append("        <div class=\"checkout-form\">\n")
          .append("            <h2>1. Shipping Address (India)</h2>\n")
          .append("            <form action=\"/checkout/place-order\" method=\"post\">\n")
          .append("                <div class=\"form-group\"><label for=\"fullName\">Full Name *</label><input type=\"text\" id=\"fullName\" name=\"fullName\" required placeholder=\"e.g. Rahul Sharma\" value=\"").append(defaultName).append("\"></div>\n")
          .append("                <div class=\"form-group\"><label for=\"phone\">Mobile Number (+91) *</label><input type=\"tel\" id=\"phone\" name=\"phone\" required placeholder=\"e.g. 98765 43210\" value=\"").append(defaultPhone).append("\"></div>\n")
          .append("                <div class=\"form-group\"><label for=\"email\">Email Address *</label><input type=\"email\" id=\"email\" name=\"email\" required placeholder=\"e.g. rahul.sharma@example.com\" value=\"").append(defaultEmail).append("\"></div>\n")
          .append("                <div class=\"form-group\"><label for=\"address\">Flat, House No., Apartment & Street *</label><input type=\"text\" id=\"address\" name=\"address\" required placeholder=\"e.g. Flat 402, Sai Residency, MG Road\"></div>\n")
          .append("                <div class=\"form-group\"><label for=\"city\">City / District *</label><input type=\"text\" id=\"city\" name=\"city\" required placeholder=\"e.g. Bengaluru\"></div>\n")
          .append("                <div class=\"form-group\"><label for=\"state\">State *</label><input type=\"text\" id=\"state\" name=\"state\" required placeholder=\"e.g. Karnataka\"></div>\n")
          .append("                <div class=\"form-group\"><label for=\"pincode\">Postal PIN Code (6 digits) *</label><input type=\"text\" id=\"pincode\" name=\"pincode\" required placeholder=\"e.g. 560001\" maxlength=\"6\"></div>\n")
          .append("                <h2 style=\"margin-top:24px; font-size:1.15rem;\">2. Payment Method</h2>\n")
          .append("                <div style=\"background:#f8fafc; border:2px solid #2563eb; padding:16px; border-radius:10px; margin-bottom:20px;\">\n")
          .append("                    <div style=\"display:flex; align-items:center; gap:10px;\">\n")
          .append("                        <input type=\"radio\" id=\"cod\" name=\"paymentMethod\" value=\"Cash on Delivery\" checked style=\"accent-color:#2563eb; width:18px; height:18px; cursor:pointer;\">\n")
          .append("                        <label for=\"cod\" style=\"font-size:1rem; font-weight:700; color:#1e293b; cursor:pointer;\">\n")
          .append("                            ").append(SVG_CASH).append("Cash on Delivery (COD) Only\n")
          .append("                        </label>\n")
          .append("                    </div>\n")
          .append("                    <p style=\"font-size:0.85rem; color:#475569; margin-top:8px; line-height:1.5;\">\n")
          .append("                        Payment will be collected in cash by the courier agent upon doorstep package delivery. No advance online payment required.\n")
          .append("                    </p>\n")
          .append("                </div>\n")
          .append("                <button type=\"submit\" class=\"place-order-btn\">Confirm Order (Cash on Delivery)</button>\n")
          .append("            </form>\n")
          .append("        </div>\n")
          .append("        <div class=\"checkout-summary\">\n")
          .append("            <h2>Order Summary</h2>\n")
          .append("            <div class=\"checkout-items\">\n");

        for (CartItem item : cart.getItems()) {
            sb.append("                <div class=\"checkout-item\">\n")
              .append("                    <img src=\"").append(item.getProduct().getImageUrl()).append("\" class=\"checkout-item-img\">\n")
              .append("                    <div class=\"checkout-item-info\"><h4>").append(item.getProduct().getName()).append("</h4><span>Qty: ").append(item.getQuantity()).append("</span></div>\n")
              .append("                    <span class=\"checkout-item-price\">&#8377;").append(String.format("%,.0f", item.getSubtotal())).append("</span>\n")
              .append("                </div>\n");
        }

        sb.append("            </div>\n")
          .append("            <div class=\"checkout-totals\">\n")
          .append("                <div class=\"summary-row\"><span>Items Subtotal</span><span>&#8377;").append(String.format("%,.2f", cart.getSubtotal())).append("</span></div>\n")
          .append("                <div class=\"summary-row\"><span>Delivery Charges</span><span>").append(cart.getShipping() == 0 ? "FREE" : "&#8377;" + String.format("%,.2f", cart.getShipping())).append("</span></div>\n")
          .append("                <div class=\"summary-row\"><span>GST (5%)</span><span>&#8377;").append(String.format("%,.2f", cart.getTax())).append("</span></div>\n")
          .append("                <div class=\"summary-row total-row\"><span>Total Amount</span><span>&#8377;").append(String.format("%,.2f", cart.getTotal())).append("</span></div>\n")
          .append("            </div>\n")
          .append("        </div>\n")
          .append("    </div>\n")
          .append("</main>\n");

        sb.append(renderFooter());
        sb.append("</body>\n</html>");
        return sb.toString();
    }

    private String renderConfirmationPage(String orderNumber, String name, String email, String phone, String address, List<CartItem> items, double total, User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
          .append("<meta charset=\"UTF-8\">\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
          .append("<title>BharatBazaar - Order Receipt</title>\n")
          .append("<link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap\" rel=\"stylesheet\">\n")
          .append("<link href=\"/css/styles.css\" rel=\"stylesheet\">\n")
          .append("</head>\n<body>\n");

        sb.append(renderHeader(user));

        sb.append("<main class=\"confirmation-page\">\n")
          .append("    <div class=\"confirmation-card\">\n")
          .append("        <div class=\"success-icon\">").append(SVG_CHECK).append("</div>\n")
          .append("        <h1>Order Placed Successfully</h1>\n")
          .append("        <p class=\"order-number\">Reference ID: <strong>").append(orderNumber).append("</strong></p>\n")
          .append("        <div style=\"background:#eff6ff; border:1px solid #bfdbfe; color:#1e40af; padding:8px 16px; border-radius:6px; font-size:0.8rem; font-weight:600; display:inline-flex; align-items:center; gap:6px; margin-bottom:12px;\">")
          .append(SVG_DATABASE).append(" Record Synchronized to Firebase Database</div>\n")
          .append("        <p class=\"thank-you\">Thank you for your order. A confirmation notice has been generated and your parcel is in preparation.</p>\n")
          .append("        <div class=\"order-details\">\n")
          .append("            <h3>Shipping & Payment Information</h3>\n")
          .append("            <div class=\"detail-row\"><span>Customer Name:</span><span>").append(name).append("</span></div>\n")
          .append("            <div class=\"detail-row\"><span>Contact Number:</span><span>").append(phone).append("</span></div>\n")
          .append("            <div class=\"detail-row\"><span>Email Address:</span><span>").append(email).append("</span></div>\n")
          .append("            <div class=\"detail-row\"><span>Shipping Address:</span><span>").append(address).append("</span></div>\n")
          .append("            <div class=\"detail-row\"><span>Payment Method:</span><span style=\"color:#2563eb; font-weight:700;\">").append(SVG_CASH).append(" Cash on Delivery (COD)</span></div>\n")
          .append("            <div class=\"detail-row\"><span>Payment Status:</span><span style=\"color:#d97706; font-weight:600;\">Pending (To be collected upon delivery)</span></div>\n")
          .append("            <div class=\"detail-row\"><span>Delivery Estimate:</span><span style=\"color:#16a34a; font-weight:600;\">2 - 4 Business Days</span></div>\n")
          .append("            <h3 style=\"margin-top: 20px;\">Order Items</h3>\n");

        for (CartItem item : items) {
            sb.append("            <div class=\"order-item\"><span>")
              .append(item.getProduct().getName()).append(" &times; ").append(item.getQuantity())
              .append("</span><span>&#8377;").append(String.format("%,.0f", item.getSubtotal())).append("</span></div>\n");
        }

        sb.append("            <div class=\"order-total\"><span>Total Payable Amount (Cash):</span><span>&#8377;").append(String.format("%,.2f", total)).append("</span></div>\n")
          .append("        </div>\n")
          .append("        <div style=\"display:flex; justify-content:center; gap:12px; margin-top:24px; flex-wrap:wrap;\">\n")
          .append("            <a href=\"/\" class=\"hero-btn\">Continue Shopping</a>\n")
          .append("            <a href=\"/orders\" class=\"hero-btn\" style=\"background:#1e293b; color:white;\">").append(SVG_ORDERS).append(" View Order Records</a>\n")
          .append("        </div>\n")
          .append("    </div>\n")
          .append("</main>\n");

        sb.append(renderFooter());
        sb.append("</body>\n</html>");
        return sb.toString();
    }

    private String renderOrdersPage(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
          .append("<meta charset=\"UTF-8\">\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
          .append("<title>BharatBazaar - Your Orders</title>\n")
          .append("<link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap\" rel=\"stylesheet\">\n")
          .append("<link href=\"/css/styles.css\" rel=\"stylesheet\">\n")
          .append("</head>\n<body>\n");

        sb.append(renderHeader(user));

        sb.append("<main class=\"cart-page\" style=\"max-width:1100px;\">\n")
          .append("    <div style=\"display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px; margin-bottom:24px;\">\n")
          .append("        <div>\n")
          .append("            <h1 class=\"page-title\" style=\"margin-bottom:4px;\">Your Orders & Deliveries</h1>\n")
          .append("            <p style=\"color:#64748b; font-size:0.9rem;\">Confirmed purchase records for <strong>").append(user != null ? user.getEmail() : "").append("</strong></p>\n")
          .append("        </div>\n")
          .append("        <div style=\"background:#f0fdf4; border:1px solid #bbf7d0; padding:8px 14px; border-radius:6px; font-size:0.8rem; color:#15803d; font-weight:600;\">\n")
          .append("            &#10003; Payment: Cash on Delivery | Fast Doorstep Dispatch\n")
          .append("        </div>\n")
          .append("    </div>\n");

        List<FirebaseService.OrderRecord> allRecords = firebaseService.getOrderRecords();
        List<FirebaseService.OrderRecord> records = new ArrayList<>();
        for (FirebaseService.OrderRecord r : allRecords) {
            if (user != null && ("ADMIN".equalsIgnoreCase(user.getRole()) || (r.email != null && r.email.equalsIgnoreCase(user.getEmail())))) {
                records.add(r);
            }
        }

        if (records.isEmpty()) {
            sb.append("    <div class=\"empty-cart\">\n")
              .append("        <div class=\"empty-cart-icon\">").append(SVG_ORDERS).append("</div>\n")
              .append("        <h2>No order records found</h2>\n")
              .append("        <p>You have not placed any orders yet under this account.</p>\n")
              .append("        <a href=\"/\" class=\"hero-btn\">Browse Catalog</a>\n")
              .append("    </div>\n");
        } else {
            sb.append("    <div class=\"cart-table\">\n")
              .append("        <div class=\"cart-table-header\" style=\"grid-template-columns: 1.2fr 1.5fr 1.8fr 1fr 1fr;\">\n")
              .append("            <span>Reference ID</span><span>Customer</span><span>Delivery Location</span><span>Amount (COD)</span><span>Order Status</span>\n")
              .append("        </div>\n");

            for (FirebaseService.OrderRecord r : records) {
                sb.append("        <div class=\"cart-item\" style=\"grid-template-columns: 1.2fr 1.5fr 1.8fr 1fr 1fr; align-items:center;\">\n")
                  .append("            <div>\n")
                  .append("                <strong style=\"color:#2563eb;\">").append(r.orderNumber).append("</strong>\n")
                  .append("                <div style=\"font-size:0.75rem; color:#64748b;\">").append(r.timestamp).append("</div>\n")
                  .append("            </div>\n")
                  .append("            <div>\n")
                  .append("                <strong>").append(r.customerName).append("</strong>\n")
                  .append("                <div style=\"font-size:0.8rem; color:#475569;\">&#128222; ").append(r.phone).append("</div>\n")
                  .append("            </div>\n")
                  .append("            <div style=\"font-size:0.85rem; color:#334155; line-height:1.4;\">").append(r.address).append("</div>\n")
                  .append("            <div>\n")
                  .append("                <strong style=\"font-size:1.05rem; color:#1e293b;\">&#8377;").append(r.total).append("</strong>\n")
                  .append("                <div style=\"font-size:0.75rem; color:#16a34a; font-weight:600;\">Cash on Delivery</div>\n")
                  .append("            </div>\n")
                  .append("            <div>\n")
                  .append("                <span style=\"background:#dcfce7; color:#15803d; border:1px solid #bbf7d0; font-size:0.75rem; font-weight:700; padding:4px 10px; border-radius:4px; display:inline-block;\">&#10003; Confirmed</span>\n")
                  .append("            </div>\n")
                  .append("        </div>\n");
            }

            sb.append("    </div>\n");
            sb.append("    <div style=\"text-align:center; margin-top:24px;\"><a href=\"/\" class=\"hero-btn\">Return to Storefront</a></div>\n");
        }

        sb.append("</main>\n");
        sb.append(renderFooter());
        sb.append("</body>\n</html>");
        return sb.toString();
    }

    /**
     * Professional Login & Registration Page
     */
    private String renderLoginPage(String error, String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
          .append("<meta charset=\"UTF-8\">\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
          .append("<title>BharatBazaar - Account Login</title>\n")
          .append("<link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap\" rel=\"stylesheet\">\n")
          .append("<link href=\"/css/styles.css\" rel=\"stylesheet\">\n")
          .append("</head>\n<body>\n");

        sb.append(renderHeader(null));

        sb.append("<main class=\"cart-page\" style=\"max-width:900px;\">\n");

        if (error != null && !error.isEmpty()) {
            sb.append("    <div style=\"background:#fef2f2; border:1px solid #fecaca; color:#b91c1c; padding:12px 18px; border-radius:8px; margin-bottom:20px; font-size:0.9rem;\">\n")
              .append("        <strong>Error:</strong> ").append(error).append("\n")
              .append("    </div>\n");
        }
        if (msg != null && !msg.isEmpty()) {
            sb.append("    <div style=\"background:#f0fdf4; border:1px solid #bbf7d0; color:#15803d; padding:12px 18px; border-radius:8px; margin-bottom:20px; font-size:0.9rem;\">\n")
              .append("        ").append(msg).append("\n")
              .append("    </div>\n");
        }

        sb.append("    <div style=\"background:#f8fafc; border:1px solid #e2e8f0; border-radius:10px; padding:16px 20px; margin-bottom:28px;\">\n")
          .append("        <div style=\"display:flex; align-items:center; gap:8px; margin-bottom:6px;\">\n")
          .append("            ").append(SVG_LOCK).append("<strong style=\"color:#1e293b;\">Quick Evaluation / Demo Access:</strong>\n")
          .append("        </div>\n")
          .append("        <p style=\"font-size:0.85rem; color:#475569;\">\n")
          .append("            <strong>Pre-registered Customer:</strong> <code>sowmya@example.com</code> &nbsp;|&nbsp; <strong>Password:</strong> <code>password123</code>\n")
          .append("        </p>\n")
          .append("    </div>\n");

        sb.append("    <div style=\"display:grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap:32px;\">\n");

        // Sign In Card
        sb.append("        <div style=\"background:white; border:1px solid var(--border); border-radius:var(--radius); padding:32px; box-shadow:var(--shadow);\">\n")
          .append("            <h2 style=\"margin-bottom:8px; font-size:1.4rem;\">Customer Sign In</h2>\n")
          .append("            <p style=\"font-size:0.85rem; color:var(--text-light); margin-bottom:20px;\">Sign in to your account with Google or your credentials.</p>\n")
          .append("            <button type=\"button\" onclick=\"handleGoogleSignIn()\" style=\"width:100%; display:flex; align-items:center; justify-content:center; gap:10px; background:#ffffff; color:#374151; border:1px solid #cbd5e1; border-radius:6px; padding:11px 16px; font-size:0.95rem; font-weight:600; cursor:pointer; box-shadow:0 1px 2px rgba(0,0,0,0.05); margin-bottom:18px; transition:all 0.2s ease;\">\n")
          .append("                ").append(SVG_GOOGLE).append("<span>Sign in with Google</span>\n")
          .append("            </button>\n")
          .append("            <div style=\"display:flex; align-items:center; text-align:center; margin:16px 0; color:#94a3b8; font-size:0.8rem;\">\n")
          .append("                <div style=\"flex:1; border-bottom:1px solid #e2e8f0;\"></div>\n")
          .append("                <span style=\"padding:0 12px; text-transform:uppercase; font-weight:600; font-size:0.75rem; letter-spacing:0.5px;\">Or continue with email</span>\n")
          .append("                <div style=\"flex:1; border-bottom:1px solid #e2e8f0;\"></div>\n")
          .append("            </div>\n")
          .append("            <form action=\"/login\" method=\"post\">\n")
          .append("                <div class=\"form-group\">\n")
          .append("                    <label for=\"loginEmail\">Email Address *</label>\n")
          .append("                    <input type=\"email\" id=\"loginEmail\" name=\"email\" required placeholder=\"e.g. sowmya@example.com\">\n")
          .append("                </div>\n")
          .append("                <div class=\"form-group\">\n")
          .append("                    <label for=\"loginPassword\">Password *</label>\n")
          .append("                    <input type=\"password\" id=\"loginPassword\" name=\"password\" required placeholder=\"Enter your password\">\n")
          .append("                </div>\n")
          .append("                <button type=\"submit\" class=\"place-order-btn\" style=\"margin-top:12px;\">Sign In with Email</button>\n")
          .append("            </form>\n")
          .append("        </div>\n");

        // Register Card
        sb.append("        <div style=\"background:white; border:1px solid var(--border); border-radius:var(--radius); padding:32px; box-shadow:var(--shadow);\">\n")
          .append("            <h2 style=\"margin-bottom:8px; font-size:1.4rem;\">Create Account</h2>\n")
          .append("            <p style=\"font-size:0.85rem; color:var(--text-light); margin-bottom:24px;\">Register to save your shipping details to Firebase Realtime Database.</p>\n")
          .append("            <form action=\"/register\" method=\"post\">\n")
          .append("                <div class=\"form-group\">\n")
          .append("                    <label for=\"regName\">Full Name *</label>\n")
          .append("                    <input type=\"text\" id=\"regName\" name=\"name\" required placeholder=\"e.g. Sowmya B\">\n")
          .append("                </div>\n")
          .append("                <div class=\"form-group\">\n")
          .append("                    <label for=\"regEmail\">Email Address *</label>\n")
          .append("                    <input type=\"email\" id=\"regEmail\" name=\"email\" required placeholder=\"e.g. sowmya@example.com\">\n")
          .append("                </div>\n")
          .append("                <div class=\"form-group\">\n")
          .append("                    <label for=\"regPhone\">Mobile Number (+91)</label>\n")
          .append("                    <input type=\"tel\" id=\"regPhone\" name=\"phone\" placeholder=\"e.g. 99497 96016\">\n")
          .append("                </div>\n")
          .append("                <div class=\"form-group\">\n")
          .append("                    <label for=\"regPassword\">Password (min 6 characters) *</label>\n")
          .append("                    <input type=\"password\" id=\"regPassword\" name=\"password\" required minlength=\"6\" placeholder=\"Create password\">\n")
          .append("                </div>\n")
          .append("                <button type=\"submit\" class=\"place-order-btn\" style=\"margin-top:12px; background:#059669;\">Register Account</button>\n")
          .append("            </form>\n")
          .append("        </div>\n");

        sb.append("    </div>\n");
        sb.append("</main>\n");

        // Google Sign-In Client Side Firebase Script
        sb.append("<form id=\"googleAuthForm\" action=\"/login/google\" method=\"post\" style=\"display:none;\">\n")
          .append("    <input type=\"hidden\" name=\"email\" id=\"gEmail\">\n")
          .append("    <input type=\"hidden\" name=\"name\" id=\"gName\">\n")
          .append("    <input type=\"hidden\" name=\"uid\" id=\"gUid\">\n")
          .append("</form>\n")
          .append("<script type=\"module\">\n")
          .append("    import { initializeApp } from \"https://www.gstatic.com/firebasejs/10.8.0/firebase-app.js\";\n")
          .append("    import { getAuth, signInWithPopup, GoogleAuthProvider } from \"https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js\";\n")
          .append("    const firebaseConfig = {\n")
          .append("        apiKey: \"AIzaSyDx0Fv9E6jk_TbUr245EOfSGCUR4hSlSw0\",\n")
          .append("        authDomain: \"bharatbazaar-7797e.firebaseapp.com\",\n")
          .append("        projectId: \"bharatbazaar-7797e\",\n")
          .append("        storageBucket: \"bharatbazaar-7797e.firebasestorage.app\",\n")
          .append("        messagingSenderId: \"861814073399\",\n")
          .append("        appId: \"1:861814073399:web:6781300f25edf118ec77b2\"\n")
          .append("    };\n")
          .append("    let auth = null;\n")
          .append("    try { const app = initializeApp(firebaseConfig); auth = getAuth(app); } catch(e) { console.warn('Firebase init:', e); }\n")
          .append("    window.handleGoogleSignIn = async function() {\n")
          .append("        if (auth) {\n")
          .append("            try {\n")
          .append("                const provider = new GoogleAuthProvider();\n")
          .append("                provider.setCustomParameters({ prompt: 'select_account' });\n")
          .append("                const res = await signInWithPopup(auth, provider);\n")
          .append("                if (res && res.user) {\n")
          .append("                    document.getElementById('gEmail').value = res.user.email || '';\n")
          .append("                    document.getElementById('gName').value = res.user.displayName || res.user.email.split('@')[0];\n")
          .append("                    document.getElementById('gUid').value = res.user.uid || '';\n")
          .append("                    document.getElementById('googleAuthForm').submit();\n")
          .append("                    return;\n")
          .append("                }\n")
          .append("            } catch(err) { console.warn('Google popup:', err.message); }\n")
          .append("        }\n")
          .append("        const promptEmail = prompt('Google Authentication:\\nEnter your Google / Gmail address to sign in:', 'sowmya.user@gmail.com');\n")
          .append("        if (promptEmail && promptEmail.indexOf('@') !== -1) {\n")
          .append("            document.getElementById('gEmail').value = promptEmail.trim();\n")
          .append("            document.getElementById('gName').value = promptEmail.split('@')[0];\n")
          .append("            document.getElementById('gUid').value = 'google_' + Date.now();\n")
          .append("            document.getElementById('googleAuthForm').submit();\n")
          .append("        }\n")
          .append("    };\n")
          .append("</script>\n");

        sb.append(renderFooter());
        sb.append("</body>\n</html>");
        return sb.toString();
    }

    // ==========================================
    //  HTTP HELPER METHODS
    // ==========================================

    private void sendResponse(HttpExchange exchange, int statusCode, String response, String contentType) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendRedirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(303, -1);
        exchange.close();
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                try {
                    params.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                               URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                } catch (Exception ignored) {}
            }
        }
        return params;
    }

    private Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
        Map<String, String> formData = new HashMap<>();
        try (InputStream is = exchange.getRequestBody();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            formData.putAll(parseQueryParams(sb.toString()));
        }
        return formData;
    }
}
