FROM gradle:8.5.0-jdk21-alpine AS build

WORKDIR /app

COPY . .

RUN gradle build --no-daemon -x test

FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar" ]