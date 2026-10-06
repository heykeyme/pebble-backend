# Stage 1: Build & Dependencies
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# 1. Install root parent POM non-recursively into local .m2 (ignores sibling services)
COPY pom.xml ./pom.xml
RUN mvn install -N -DskipTests

# 2. Build & Install shared common library into local .m2
COPY libs/common ./libs/common
RUN mvn clean install -f libs/common/pom.xml -DskipTests

# 3. Copy target microservice code
COPY svc/acc/lgi ./svc/acc/lgi

# 4. Package only the target microservice (it resolves libs/common and root pom from local .m2)
RUN mvn clean package -f svc/acc/lgi/pom.xml -DskipTests

# Stage 2: Lean Production Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy final fat JAR
COPY --from=builder /app/svc/acc/lgi/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]