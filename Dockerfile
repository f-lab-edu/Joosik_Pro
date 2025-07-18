FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY build/libs/Joosik_Pro-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=ec2

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
