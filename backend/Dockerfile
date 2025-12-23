# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml and download dependencies offline
COPY pom.xml ./
RUN mvn -q -e -DskipTests dependency:go-offline

# Copy source and build jar
COPY src ./src
RUN mvn -q -e -DskipTests package

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-alpine

# Install required tools: bash, curl, gzip, postgresql-client, mysql-client, rclone
RUN apk add --no-cache \
        bash \
        curl \
        gzip \
        postgresql-client \
        mysql-client \
        unzip \
        ca-certificates \
    && curl -Of https://downloads.rclone.org/rclone-current-linux-amd64.zip \
    && unzip rclone-current-linux-amd64.zip \
    && cp rclone-*-linux-amd64/rclone /usr/local/bin/ \
    && chmod 755 /usr/local/bin/rclone \
    && rm -rf rclone-*-linux-amd64*

# Create app user and directories
RUN addgroup -S appgroup && adduser -S appuser -G appgroup \
    && mkdir -p /app/uploads /app/backups \
    && chown -R appuser:appgroup /app

WORKDIR /app
COPY --from=builder /app/target/*.jar /app/app.jar

USER appuser

# Copy RCLONE_CONFIG environment variable to file
# Add in ENTRYPOINT
ENTRYPOINT ["sh", "-c", "echo \"$RCLONE_CONFIG\" > /app/rclone.conf && java ${JAVA_OPTS} -jar /app/app.jar"]
