ARG JAR_FILE=target/*.jar
FROM baitsuatimages.azurecr.io/baitsuatimages/openjdk:19-jdk-alpine
WORKDIR /app
# use the arg value provided by the build command (must be exact path)
ARG JAR_FILE
COPY ${JAR_FILE} /app/app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]

