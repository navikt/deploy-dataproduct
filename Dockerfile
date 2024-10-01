FROM gcr.io/distroless/java21-debian12:nonroot

COPY build/libs/*.jar /app/

ENV LOG_FORMAT="logstash"
ENV JAVA_OPTS='-XX:MaxRAMPercentage=90'
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/oom-dump.hprof"

WORKDIR /app

CMD ["app.jar"]