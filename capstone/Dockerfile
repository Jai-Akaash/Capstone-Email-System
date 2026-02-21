# ==========================================
# STAGE 1: Build the application
# ==========================================
# Use an official Maven image to build the app
FROM maven:3.9-eclipse-temurin-17 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and the source code to the container
COPY pom.xml .
COPY src ./src

# Package the application (skip tests here since Jenkins will run them)
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: Run the application
# ==========================================
# Use the standard JRE image that supports Mac Apple Silicon natively
FROM eclipse-temurin:17-jre

# Set the working directory
WORKDIR /app

# Copy ONLY the built .jar file from Stage 1 into this new container
COPY --from=builder /app/target/*.jar app.jar

# Tell Docker that this containers listens on port 8080
EXPOSE 8080

# The command to start the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]