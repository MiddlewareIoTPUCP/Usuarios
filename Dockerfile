FROM maven:3.6.3-openjdk-11 AS MAVEN_BUILD

COPY pom.xml /build/
COPY src /build/src/

WORKDIR /build/
RUN mvn clean package -Dmaven.test.skip=true

#Setting openjdk-server
FROM openjdk:11-jdk-slim

WORKDIR /app

COPY --from=MAVEN_BUILD /build/target/usuarios-0.0.1.jar /app/

ENTRYPOINT ["java", "-jar", "usuarios-0.0.1.jar"]