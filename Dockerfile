FROM eclipse-temurin:17

COPY build/libs/*.jar ./

ENV LOG_FORMAT="logstash"

RUN mkdir -p /tmp/.java/.systemPrefs && \
    mkdir -p /tmp/.java/.userPrefs && \
    chmod -R 755 /tmp/.java && \
    chown -R 1069:1069 /tmp/.java

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/oom-dump.hprof \
               -Djava.util.prefs.systemRoot=/tmp/.java/.systemPrefs \
               -Djava.util.prefs.userRoot=/tmp/.java/.userPrefs"

RUN mkdir -p /tmp/init-scripts && \
    echo 'java -XX:MaxRAMPercentage=75 -XX:+PrintFlagsFinal -version | grep -Ei "maxheapsize|maxram"' > /tmp/init-scripts/0-dump-memory-config.sh && \
    chmod +x /tmp/init-scripts/0-dump-memory-config.sh && \
    chown -R 1069:1069 /tmp/init-scripts

# Set the working directory
WORKDIR /app

# Set the user to non-root with user and group ID 1069
USER 1069:1069