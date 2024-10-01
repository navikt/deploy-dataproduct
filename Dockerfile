FROM eclipse-temurin:17-jdk

COPY build/libs/*.jar /app/.

ENV LOG_FORMAT="logstash"

RUN mkdir -p /tmp/.java/.systemPrefs && \
    mkdir -p /tmp/.java/.userPrefs && \
    chmod -R 755 /tmp/.java

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/oom-dump.hprof \
               -Djava.util.prefs.systemRoot=/tmp/.java \
               -Djava.util.prefs.userRoot=/tmp/.java/.userPrefs"

RUN mkdir -p /tmp/init-scripts
RUN echo 'java -XX:MaxRAMPercentage=75 -XX:+PrintFlagsFinal -version | grep -Ei "maxheapsize|maxram"' > /tmp/init-scripts/0-dump-memory-config.sh
RUN chmod +x /tmp/init-scripts/0-dump-memory-config.sh