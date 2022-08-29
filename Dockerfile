FROM openjdk:11-jre
COPY target/*.jar /app.jar
ENTRYPOINT java ${JAVA_OPTS} -jar /app.war