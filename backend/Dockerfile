FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src src
RUN mvn -B package
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S apiwatch && adduser -S apiwatch -G apiwatch
COPY --from=build /app/target/apiwatch-1.0.0.jar app.jar
USER apiwatch
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
