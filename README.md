@"
# Automated Return and Refund Management Portal

A web application where customers raise product return requests, support agents and finance managers move each request through a controlled approval workflow, and managers see a live summary dashboard.

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
"@ | Out-File -Encoding utf8 README.md

git checkout develop
git pull origin develop