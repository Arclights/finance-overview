FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Cache the Gradle distribution and dependencies separately from the sources
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null

COPY src ./src
RUN ./gradlew --no-daemon shadowJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
