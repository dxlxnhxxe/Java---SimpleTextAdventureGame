# Stage 1: Build JAR with Maven and OpenJDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# Copy Maven descriptor and download dependencies
COPY pom.xml ./
COPY libs ./libs/
RUN mvn dependency:go-offline -B || true

# Copy source and config files
COPY src ./src
COPY config ./config

# Package application jar
RUN mvn clean package -DskipTests

# Stage 2: Minimal Production JRE Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S stag && adduser -S stag -G stag
USER stag:stag

# Copy application artifact and configs
COPY --from=builder /build/target/cw-stag-*.jar app.jar
COPY --from=builder /build/config ./config

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
