# Etapa de construcción
FROM gradle:8.1.1-jdk17-jammy AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle bootJar --no-daemon

# Etapa de producción
FROM eclipse-temurin:17.0.7_7-jre-alpine
RUN mkdir /app

# Exponer el puerto 8080
EXPOSE 8080

COPY --from=build /home/gradle/src/build/libs/*.jar /app/examguard.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/examguard.jar", "--host=0.0.0.0"]