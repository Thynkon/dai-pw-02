# Base image
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Download build dependencies
COPY mvnw pom.xml dependency-reduced-pom.xml ./
COPY .mvn ./.mvn
RUN chmod +x ./mvnw && ./mvnw install

# Copy project and build
COPY src ./src
RUN ./mvnw package

FROM eclipse-temurin:21-jre AS prod

WORKDIR /app

COPY --from=builder /app/target/simpft-1.0.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

CMD ["--help"]
