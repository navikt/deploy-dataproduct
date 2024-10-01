FROM gcr.io/distroless/java21-debian12:nonroot

COPY build/libs/*.jar ./

ENV LOG_FORMAT="logstash"
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/oom-dump.hprof"