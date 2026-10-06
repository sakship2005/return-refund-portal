# Automated Return and Refund Management Portal

A web application where customers raise return requests, support agents and finance managers move them through a controlled workflow, and managers monitor the summary dashboard.

## Tech Stack
- Java 17, Spring Boot 3.2.5 (upgraded and verified working)
- Spring MVC + Thymeleaf
- Spring Data JPA, H2 (dev)
- Maven
- Tomcat (deployment target)

## Status Workflow
Requested -> Under Review -> Approved / Rejected -> Item Received -> Refund Processed -> Closed

## Running Locally
``mvn spring-boot:run``
App: http://localhost:8081
Health check: http://localhost:8081/health

## MVP Scope
See docs/mvp-scope.md for the full 15-task scope.

## Branching
- main        : stable, release-ready
- develop     : integration branch
- feature/<name> : one branch per feature, merged into develop via PR

# Base image with Java 21 runtime
FROM eclipse-temurin:21-jre-alpine

# Set working directory inside container
WORKDIR /app

# Copy the packaged Spring Boot WAR file into container
COPY target/rrp.war app.war

# Expose internal application port
EXPOSE 8080

# Run the executable WAR file
ENTRYPOINT ["java", "-jar", "app.war"]