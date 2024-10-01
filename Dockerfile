FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app
COPY build/libs/*.jar /app/

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/oom-dump.hprof"

WORKDIR /app

CMD ["app.jar"]