FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/rrp.war app.war
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.war"]