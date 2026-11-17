# Etapa 1: compilar el proyecto con Maven y generar el .jar
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

# Etapa 2: imagen liviana solo con el JRE y el .jar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Hora de Colombia: los festivos y el cierre de caja dependen de la fecha local
ENV TZ=America/Bogota
# Limita la memoria de la JVM para el plan gratuito de Render (512 MB)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Duser.timezone=America/Bogota"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
