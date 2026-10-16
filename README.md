# BharatBazaar — Enterprise Java E-Commerce Platform

> 🌐 **Live Deployed Website**: [https://bshravani-11.github.io/Bharatbazaar](https://bshravani-11.github.io/Bharatbazaar)  
> 📦 **GitHub Repository**: [https://github.com/Bshravani-11/Bharatbazaar](https://github.com/Bshravani-11/Bharatbazaar)

A professional, full-featured Indian E-Commerce and Shopping Cart web application with modern HTML5/CSS3 frontend, SVG vector iconography, user authentication, and real-time cloud data persistence backed by **Firebase Realtime Database**.

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

The application will automatically compile and launch the server in your default browser.

---

## ☁️ Live Deployment

### 1. GitHub Pages (Active Live Deployment)
The storefront frontend is bundled and deployed live directly via **GitHub Pages**:
- **Live URL**: [https://bshravani-11.github.io/Bharatbazaar](https://bshravani-11.github.io/Bharatbazaar)
- Includes live Firebase Authentication (Email/Password & Google Sign-In) and real-time cloud data storage via Firebase Realtime Database.

### 2. Full Java Backend Cloud Hosting (Render / Docker)
To host the full-stack pure Java backend server container in the cloud:
1. Sign in to **[Render.com](https://render.com/)** with your GitHub account (`Bshravani-11`).
2. Click **New +** > **Web Service** and connect the `Bharatbazaar` repository.
3. Select **Docker** environment (Render detects the included `Dockerfile` and `render.yaml`).
4. Choose the **Free** instance tier and click **Deploy Web Service**.

---

## 🌐 Live Application Endpoints

- **Storefront & Catalog**: [https://bshravani-11.github.io/Bharatbazaar](https://bshravani-11.github.io/Bharatbazaar)
- **Shopping Cart View**: [https://bshravani-11.github.io/Bharatbazaar#cart](https://bshravani-11.github.io/Bharatbazaar#cart)
- **Order Checkout (COD)**: [https://bshravani-11.github.io/Bharatbazaar#checkout](https://bshravani-11.github.io/Bharatbazaar#checkout)
- **Order History & Tracking**: [https://bshravani-11.github.io/Bharatbazaar#orders](https://bshravani-11.github.io/Bharatbazaar#orders)
- **Customer Account Portal**: [https://bshravani-11.github.io/Bharatbazaar#login](https://bshravani-11.github.io/Bharatbazaar#login)

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
