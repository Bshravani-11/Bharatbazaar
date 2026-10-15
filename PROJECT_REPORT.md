# ACADEMIC PROJECT REPORT

## PROJECT TITLE:
### **BHARATBAZAAR — FULL-STACK JAVA E-COMMERCE & ORDER MANAGEMENT PLATFORM WITH FIREBASE REALTIME CLOUD INTEGRATION**

**Degree:** Bachelor of Technology (B.Tech) — 2nd Year  
**Department:** Computer Science and Engineering / Information Technology  
**Repository:** [https://github.com/Bshravani-11/Bharatbazaar](https://github.com/Bshravani-11/Bharatbazaar)  

---

## 1. ABSTRACT
Modern e-commerce platforms require responsive web interfaces, robust backend business logic, and reliable cloud persistence. **BharatBazaar** is an enterprise-grade shopping cart application developed using **Pure Java 17** without dependency on heavy external web frameworks. 

The application incorporates key Object-Oriented Programming (OOP) paradigms (Encapsulation, Composition, Collections Framework, and Separation of Concerns). It simulates real-world Indian electronic retail standards featuring Indian Rupee (INR ₹) currency formatting, a 5% Goods and Services Tax (GST) computation engine, a threshold-based free delivery subsystem (qualified at ₹499+), and an exclusive Cash on Delivery (COD) checkout pipeline. 

To achieve persistent cloud order tracking, the backend integrates asynchronously with the **Firebase Realtime Database REST API** using Java's native HTTP Client (`java.net.http.HttpClient`), providing an interactive **Order Management System (OMS)** for real-time tracking of placed orders.

---

## 2. TECHNOLOGIES USED

### 2.1 Backend Core
* **Language:** Java (JDK 17 LTS — OpenJDK Temurin 17.0.16)
* **Web Server:** Standard Library `com.sun.net.httpserver.HttpServer` (embedded lightweight HTTP server, zero third-party JARs)
* **Networking & REST Client:** Java 11+ Native HTTP Client (`java.net.http.HttpClient`, `HttpRequest`, `HttpResponse`)
* **Concurrency:** Asynchronous execution with `java.util.concurrent.CompletableFuture` for non-blocking cloud synchronization.
* **Data Structures:** Java Collections Framework (`ArrayList`, `List`, `Map`, `HashMap`).

### 2.2 Cloud & Database
* **Database:** Google Firebase Realtime Database
* **Protocol:** HTTPS REST API (`PUT /orders/{orderNumber}.json` and `GET /orders.json`)
* **Data Format:** JSON (JavaScript Object Notation)

### 2.3 Frontend & User Interface
* **Markup:** Semantic HTML5
* **Styling:** Modern CSS3 (CSS Custom Properties/Variables, CSS Grid, Flexbox, Media Queries)
* **Iconography:** Scalable Vector Graphics (Inline SVG) — no external icon fonts or casual emojis
* **Typography:** Inter (Google Fonts)

### 2.4 DevOps & Deployment
* **Version Control:** Git & GitHub ([Bshravani-11/Bharatbazaar](https://github.com/Bshravani-11/Bharatbazaar))
* **Containerization:** Multi-stage Dockerfile (`eclipse-temurin:17-jdk-alpine` build + `eclipse-temurin:17-jre-alpine` runtime)
* **Cloud Platform:** Render.com PaaS integration (`render.yaml` specification)

---

## 3. COMPLETE FILE NAMES & PROJECT DIRECTORY STRUCTURE

```
Bharatbazaar/
│
├── .gitignore                       # Specifies build artifacts and temporary files excluded from Git
├── Dockerfile                       # Multi-stage container recipe for cloud hosting (Render/Railway)
├── render.yaml                      # Cloud deployment blueprint configuration
├── firebase-config.properties       # Firebase Realtime Database connection settings
├── pom.xml                          # Optional Maven project descriptor
├── run.bat                          # One-click Windows Command Prompt build and execution script
├── run.ps1                          # PowerShell automated compilation and startup script
├── README.md                        # Project documentation and setup guidelines
├── PROJECT_REPORT.md                # Formal academic project report
│
├── bin/                             # Directory containing compiled Java bytecode (.class files)
│   └── com/shopmart/
│       ├── ShoppingCartServer*.class
│       ├── model/
│       │   ├── Cart.class
│       │   ├── CartItem.class
│       │   └── Product.class
│       └── service/
│           ├── ProductService.class
│           └── FirebaseService*.class
│
└── src/
    └── main/
        ├── java/com/shopmart/
        │   ├── ShoppingCartServer.java   # Central HTTP web server, routing, and view rendering
        │   ├── model/
        │   │   ├── Product.java          # Product domain model (Encapsulation)
        │   │   ├── CartItem.java         # Cart item entity linking Product and quantity (Composition)
        │   │   └── Cart.java             # Shopping cart logic, collection handling, and pricing
        │   └── service/
        │       ├── ProductService.java   # In-memory product catalog, category & search algorithms
        │       └── FirebaseService.java  # Firebase REST API cloud synchronization client
        │
        └── resources/
            ├── application.properties    # Server port and application configuration
            ├── static/css/
            │   └── styles.css            # Responsive layout and component styling
            └── templates/
                ├── index.html            # Reference template for storefront
                ├── cart.html             # Reference template for shopping cart
                ├── checkout.html         # Reference template for order checkout
                └── confirmation.html     # Reference template for order confirmation
```

---

## 4. DETAILED DESCRIPTION OF SOURCE FILES

| File Name | Location | Function & Purpose |
|---|---|---|
| **`ShoppingCartServer.java`** | `src/main/java/com/shopmart/` | **Central Web Server & Controller.** Initializes the embedded HTTP server on a configurable port (`PORT` env variable or 8080). Handles HTTP request routing for `/` (Storefront), `/cart` (Cart view), `/cart/add`, `/cart/increase`, `/cart/decrease`, `/cart/remove`, `/cart/clear`, `/checkout`, `/checkout/place-order`, and `/orders` (Order Management System). Contains professional SVG icon definitions and dynamic HTML view generators. |
| **`Product.java`** | `src/main/java/com/shopmart/model/` | **Domain Model Entity.** Demonstrates OOP Encapsulation. Stores attributes: `id` (Long), `name` (String), `description` (String), `price` (double, in INR), `category` (String), `imageUrl` (String), and `stock` (int). Provides constructors and public getter/setter methods. |
| **`CartItem.java`** | `src/main/java/com/shopmart/model/` | **Composition Entity.** Demonstrates OOP Composition (*HAS-A* relationship). Encapsulates a `Product` reference along with an integer `quantity`. Implements `getSubtotal()` calculating `product.getPrice() * quantity`, as well as `incrementQuantity()` and `decrementQuantity()`. |
| **`Cart.java`** | `src/main/java/com/shopmart/model/` | **Business Logic & Collection Manager.** Manages an `ArrayList<CartItem>`. Implements core business logic: `addItem()`, `removeItem()`, `updateQuantity()`, `clear()`, `getSubtotal()`, `getShipping()` (₹49 or Free if order ≥ ₹499), `getTax()` (5% GST), and `getTotal()`. Computes free delivery progress metrics. |
| **`ProductService.java`** | `src/main/java/com/shopmart/service/` | **Service Layer.** Maintains an in-memory catalog of 12 authentic Indian consumer products across diverse categories (Electronics, Fashion, Home & Kitchen, Grocery, Footwear, Accessories). Implements case-insensitive keyword searching and category filtering logic. |
| **`FirebaseService.java`** | `src/main/java/com/shopmart/service/` | **Cloud Integration Service.** Reads connection parameters from `firebase-config.properties`. Uses `java.net.http.HttpClient` to asynchronously synchronize orders to Firebase Realtime Database via `PUT /orders/{orderNumber}.json`. Contains thread-safe in-memory caching and offline resilience mechanisms. |
| **`styles.css`** | `src/main/resources/static/css/` | **Enterprise Stylesheet.** Provides corporate styling, CSS variables, CSS grid layouts for responsive cards, table design for cart and orders, clean progress bars, and modal styles. |
| **`firebase-config.properties`** | Root Directory | **Configuration.** Stores Firebase Realtime Database URL and activation flags. |
| **`run.bat` & `run.ps1`** | Root Directory | **Automation Scripts.** Automates `javac` compilation and starts the server with a single click. |
| **`Dockerfile` & `render.yaml`** | Root Directory | **DevOps & Cloud Config.** Multi-stage Docker build recipe and cloud hosting specification for Render/Railway deployment. |

---

## 5. OBJECT-ORIENTED PROGRAMMING (OOP) CONCEPTS DEMONSTRATED

1. **Encapsulation**:
   - Class fields in `Product`, `CartItem`, and `Cart` are declared `private`.
   - Data access and modifications are strictly governed via public getter and setter methods and validation logic.

2. **Composition (*HAS-A* Relationship)**:
   - `CartItem` contains a reference to a `Product` instance rather than inheriting from it.
   - `Cart` contains a dynamic collection of `CartItem` objects (`List<CartItem>`).

3. **Collections Framework & Iteration**:
   - `ArrayList` is used to implement dynamic storage for items in the cart.
   - For-each loops and predicates (`removeIf`) are employed to search, aggregate totals, and delete items cleanly.

4. **Separation of Concerns (MVC Pattern)**:
   - **Model**: `Product.java`, `CartItem.java`, `Cart.java` (Business data and logic)
   - **View**: Dynamic HTML templates in `ShoppingCartServer.java` styled with `styles.css`
   - **Controller / Service**: `ProductService.java`, `FirebaseService.java`, HTTP Handlers (Coordination, request routing, external cloud I/O)

5. **Asynchronous Concurrency**:
   - `CompletableFuture.runAsync()` is utilized in `FirebaseService` to ensure external cloud network latency never blocks the customer-facing HTTP response thread.
