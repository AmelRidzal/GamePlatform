FROM eclipse-temurin:21-jre
LABEL authors="am37"
WORKDIR /app
COPY target/tictactoeservice-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]