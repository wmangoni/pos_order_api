# Use an official OpenJDK runtime as a parent image
FROM openjdk:23.0.1-open

# Set the working directory in the container
WORKDIR /app

# Copy the fat JAR file into the container at /app
# Adjust the JAR name if it's different
COPY target/menu-service-0.0.1-SNAPSHOT.jar app.jar

# Make port 8082 available
EXPOSE 8082

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]

# To build this Docker image:
# 1. Ensure order-service-0.0.1-SNAPSHOT.jar is built (mvn package) in target/
# 2. Run from order-service directory: docker build -t order-service-app .