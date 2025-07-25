# ---- Build Stage ----
FROM gradle:8.14.3-jdk21-alpine AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle build

# ---- Package Stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]

