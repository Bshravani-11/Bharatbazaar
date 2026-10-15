# BharatBazaar — Enterprise Java E-Commerce Platform

A professional, full-featured Indian E-Commerce and Shopping Cart web application developed in **Pure Java 17** with built-in HTTP networking, modern HTML5/CSS3 frontend with SVG iconography, and real-time cloud data persistence backed by **Firebase Realtime Database**.

---

## 🌟 Key Features

- **Product Catalog & Search**:
  - Filter products by categories: *Electronics, Fashion, Home & Kitchen, Grocery, Footwear, Accessories*.
  - Real-time catalog search by keywords and brand names.
- **Shopping Cart Management**:
  - Add items, increment/decrement quantities, and delete items.
  - Automatic calculations: Unit Subtotal, 5% GST, Delivery Charges (₹49 or Free), and Grand Total.
  - Interactive **Free Delivery Progress Indicator** (qualified at ₹499+).
- **Checkout & Cash on Delivery (COD)**:
  - Indian address form validation (State, City, 6-digit PIN code).
  - Exclusive **Cash on Delivery (COD) Only** payment system.
  - Detailed digital invoice with a unique tracking reference ID (`IND-XXXXXX`).
- **Order Management System (OMS)**:
  - Centralized order portal accessible at `/orders`.
  - Every placed order is synchronized to **Firebase Realtime Database** via asynchronous REST calls (`CompletableFuture`).
  - Seamless offline fallback ensures orders are cached locally if internet connectivity is interrupted.
- **Zero Maven Overhead**:
  - Built with standard Java 17 library (`com.sun.net.httpserver.HttpServer` and `java.net.http.HttpClient`).
  - No Maven or complex external dependencies required to compile or run.

---

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| **Backend & Routing** | Pure Java 17 (`com.sun.net.httpserver.HttpServer`) |
| **Cloud Database** | Firebase Realtime Database (REST API via `java.net.http.HttpClient`) |
| **Frontend** | HTML5, CSS3, Modern SVG Vector Icons |
| **Architecture** | Model-View-Controller (MVC) with Service Layer |

---

## 📁 Project Structure

```
Bharatbazaar/
├── firebase-config.properties   # Firebase Realtime Database configuration
├── run.bat                      # One-click Windows runner
├── run.ps1                      # PowerShell runner script
├── pom.xml                      # Optional Maven configuration
├── src/main/java/com/shopmart/
│   ├── ShoppingCartServer.java  # HTTP server and page routing (/ , /cart, /checkout, /orders)
│   ├── model/
│   │   ├── Product.java         # Product entity with encapsulation
│   │   ├── CartItem.java        # Composition relationship with Product
│   │   └── Cart.java            # Shopping cart collection and business calculations
│   └── service/
│       ├── ProductService.java  # Catalog management and search filters
│       └── FirebaseService.java # Asynchronous Firebase cloud synchronization
└── src/main/resources/static/css/
    └── styles.css               # Responsive design stylesheet
```

---

## 🚀 Getting Started

### Prerequisites
- **Java Development Kit (JDK 17 or higher)** installed and added to your `PATH`.

### Running the Application

#### Option 1: PowerShell
```powershell
.\run.ps1
```

#### Option 2: Windows Command Prompt / Double-Click
```cmd
run.bat
```

The application will compile and launch the server on **http://localhost:8080**.

---

## ☁️ How to Deploy Live (Free Cloud Hosting via GitHub)

Because this application includes a full-stack **Java backend** and **HTTP server** (not just static HTML), GitHub Pages alone cannot run the Java virtual machine. 

To deploy it live with a public URL (e.g. `https://bharatbazaar.onrender.com`), use **Render.com** (100% Free, connects directly to your GitHub repo):

### Step-by-Step Deployment on Render:
1. Open **[Render.com](https://render.com/)** and sign in with your GitHub account (`Bshravani-11`).
2. Click **New +** > **Web Service**.
3. Choose **Build and deploy from a Git repository** and select **`Bharatbazaar`**.
4. Configure these simple settings:
   - **Name**: `bharatbazaar`
   - **Runtime**: `Docker` (Render will automatically detect the included `Dockerfile` and `render.yaml`)
   - **Instance Type**: **Free**
5. Click **Deploy Web Service**!
6. Render will automatically compile the Java code, start the server, and give you a **Live Public URL** (e.g. `https://bharatbazaar.onrender.com`) that anyone can access from anywhere!

---

## 🌐 Application Endpoints

- **Storefront**: [http://localhost:8080/](http://localhost:8080/)
- **Shopping Cart**: [http://localhost:8080/cart](http://localhost:8080/cart)
- **Order Checkout**: [http://localhost:8080/checkout](http://localhost:8080/checkout)
- **Order Management System**: [http://localhost:8080/orders](http://localhost:8080/orders)

---

## 🔥 Firebase Configuration

To link your own Firebase project:
1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a project.
2. Navigate to **Build > Realtime Database** and click **Create Database**.
3. Select **Test Mode** (read/write enabled).
4. Copy your database URL and paste it in `firebase-config.properties`:
   ```properties
   firebase.enabled=true
   firebase.database.url=https://your-project-id.firebaseio.com
   ```

---

## 📜 License
Academic assignment project for B.Tech Computer Science Engineering.
