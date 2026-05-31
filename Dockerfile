# ── Stage 1: Build ────────────────────────────────────────
# Use official Maven + JDK image — no need for mvnw or .mvn folder
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml first — downloads dependencies as a separate layer
# (only re-downloads when pom.xml changes, not on every code change)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the JAR
COPY src src
RUN mvn clean package -DskipTests -B

# ── Stage 2: Run ──────────────────────────────────────────
# Use slim JRE-only image — much smaller than JDK
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Copy the built JAR from Stage 1
COPY --from=build /app/target/techforum-*.jar app.jar

# Render uses PORT env variable — expose it
EXPOSE 8080

# Health check — Render uses this to know when app is ready
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/auth/health || exit 1

# Start the app with memory settings suitable for free tier (512MB RAM)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]