# Docker 镜像构建
FROM maven:3.9-amazoncorretto-17-alpine as builder

# Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build a release artifact.
RUN mvn package -DskipTests

# Run the web service on container startup.
CMD ["java","-jar","/app/target/mate-match-backend-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]