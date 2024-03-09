# Use Maven image with OpenJDK 17
FROM maven:3.8.4-openjdk-17-slim

COPY . /unitech

WORKDIR /unitech

RUN mvn clean install -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/unitechApp-0.0.1-SNAPSHOT.jar"]
