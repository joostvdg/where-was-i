FROM cgr.dev/chainguard/jre:latest

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseParallelGC -XX:ActiveProcessorCount=1"
ENV PORT=8080
ENV JDBC_DATABASE_URL="jdbc:postgresql://localhost:5432"
ENV JDBC_DATABASE_USERNAME="wherewasi"
ENV JDBC_DATABASE_PASSWORD="wherewasi"

WORKDIR /app
EXPOSE 8080
USER 1001

CMD  ["-jar","/app/app.jar"]

COPY target/*.jar app.jar

