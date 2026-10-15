# ==========================================
# Multi-stage Dockerfile for BharatBazaar
# ==========================================

# Stage 1: Build the Java application
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy source files
COPY . .

# Compile Java sources
RUN mkdir -p bin && \
    javac -d bin -encoding UTF-8 src/main/java/com/shopmart/model/*.java src/main/java/com/shopmart/service/*.java src/main/java/com/shopmart/*.java

# Stage 2: Minimal JRE runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy compiled classes and resources
COPY --from=build /app/bin ./bin
COPY --from=build /app/src/main/resources ./src/main/resources
COPY --from=build /app/firebase-config.properties ./firebase-config.properties

# Cloud platforms automatically pass PORT via environment
ENV PORT=8080
EXPOSE 8080

CMD ["java", "-cp", "bin", "com.shopmart.ShoppingCartServer"]
