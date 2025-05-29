# Use an official OpenJDK runtime as a parent image
FROM openjdk:23.0.1-open

# Set the working directory in the container
WORKDIR /app

# Copy the fat JAR file into the container at /app
# Assumes the JAR is in target/ and named menu-service-0.0.1-SNAPSHOT.jar
# Adjust the JAR name if it's different (e.g., if you change artifactId or version in pom.xml)
COPY target/menu-service-0.0.1-SNAPSHOT.jar app.jar

# Make port 8081 available to the world outside this container
EXPOSE 8081

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]

# To build this Docker image:
# 1. Ensure menu-service-0.0.1-SNAPSHOT.jar is built (mvn package) in target/
# 2. Run from menu-service directory: docker build -t menu-service-app .