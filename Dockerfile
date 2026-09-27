# Multi-stage Dockerfile for ShopEase E-Commerce Capstone Application
# Stage 1: Build application and package WAR
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml checkstyle.xml spotbugs-exclude.xml ./
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Production Tomcat 9 Container
FROM tomcat:9.0.86-jdk17-temurin-jammy
LABEL maintainer="ShopEase Engineering Team <engineer@shopease.com>"

# Remove default Tomcat demo webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy packaged WAR as ROOT.war for root context deployment (http://domain/)
COPY --from=builder /app/target/shopease.war /usr/local/tomcat/webapps/ROOT.war

# Create persistent data directory for H2 database and grant tomcat permissions
RUN mkdir -p /app/data && chmod -R 777 /app/data

# Copy and configure container entrypoint script
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

# Environment variables for production H2 persistence & Tomcat port
ENV PORT=8080
ENV DB_PATH=/app/data/shopease
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Expose default HTTP port
EXPOSE 8080

# Execute entrypoint to dynamically configure runtime PORT before launching Tomcat
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
CMD ["catalina.sh", "run"]
