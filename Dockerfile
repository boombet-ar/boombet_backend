
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app


COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

# Nota: Si la versión alpine de Java 25 da problemas de compatibilidad,
# cambia esta línea por: FROM eclipse-temurin:25-jre
FROM eclipse-temurin:25-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]