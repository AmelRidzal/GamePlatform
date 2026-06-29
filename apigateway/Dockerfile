FROM eclipse-temurin:21-jre
LABEL author="am37"
WORKDIR /app
COPY target/apigateway-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]