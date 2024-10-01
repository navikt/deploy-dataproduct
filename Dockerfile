FROM eclipse-temurin:17-jdk

COPY build/libs/*.jar /app/.

ENV LOG_FORMAT="logstash"

RUN mkdir -p /app/.java/.systemPrefs && \
    mkdir -p /tmp/.java/.userPrefs && \
    chmod -R 755 /app/.java

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/oom-dump.hprof \
               -Djava.util.prefs.systemRoot=/app/.java/ \
               -Djava.util.prefs.userRoot=/tmp/.java/.userPrefs"

RUN mkdir -p /init-scripts
RUN echo 'java -XX:MaxRAMPercentage=75 -XX:+PrintFlagsFinal -version | grep -Ei "maxheapsize|maxram"' > /init-scripts/0-dump-memory-config.sh