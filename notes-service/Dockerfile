# Etape de build : compile le jar avec Maven.
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# Etape d'execution : image JRE slim, utilisateur non-root.
FROM eclipse-temurin:17.0.13_11-jre-alpine
RUN addgroup -S medilabo && adduser -S medilabo -G medilabo
WORKDIR /app
COPY --from=build /build/target/notes-service.jar app.jar
USER medilabo
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
