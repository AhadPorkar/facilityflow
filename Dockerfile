# Multi-stage, reproducible JAR image: JS/CSS ships inside the Spring Boot executable.
FROM maven:3.9.11-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY pom.xml .
RUN mvn --batch-mode --no-transfer-progress -DskipTests dependency:go-offline
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:21-jre-noble
WORKDIR /app
RUN groupadd --system facilityflow && useradd --system --gid facilityflow --no-create-home facilityflow
COPY --from=builder /workspace/target/facilityflow-1.0.0.jar /app/facilityflow.jar
USER facilityflow
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["java","-jar","/app/facilityflow.jar"]
