# The project compiles as Java 21 on a local JDK. This image is Temurin 17,
# which Spring Boot 3.5 supports, so the build overrides the release to 17.
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

RUN sed -i 's/\r$//' mvnw \
    && chmod +x mvnw \
    && ./mvnw -B -DskipTests -Djava.version=17 package

FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

COPY --from=build /app/target/helpdesk-backend-*.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
