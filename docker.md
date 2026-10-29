@"
# Task 11: Docker Image and Container Lifecycle

## Dockerfile
Uses eclipse-temurin:21-jre-alpine running target/rrp.war on port 8081.

## Container Lifecycle Execution Log
- Build: docker build -t return-refund-portal:1.0.0 .
- Tag: docker tag return-refund-portal:1.0.0 return-refund-portal:latest
- Run: docker run -d -p 8082:8081 --name rrp-container return-refund-portal:latest
- Inspect: docker ps, docker logs rrp-container
- Stop/Start: docker stop rrp-container, docker start rrp-container
- Remove: docker rm -f rrp-container
"@ | Set-Content -Path DOCKER.md -Encoding UTF8