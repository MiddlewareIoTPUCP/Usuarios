FROM maven:3.6.3-openjdk-11 AS build

WORKDIR /app

COPY pom.xml .

# download the dependency if needed or if the pom file is changed
RUN mvn dependency:go-offline -B

COPY src src

RUN mvn package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

#Setting openjdk-server
FROM openjdk:11-jdk-slim
ARG DEPENDENCY=/app/target/dependency

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java", "-cp", "app:app/lib/*","pucp.middlewareiot.usuarios.UsuariosApplication"]
