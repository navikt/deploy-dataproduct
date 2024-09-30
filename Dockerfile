FROM eclipse-temurin:17-jdk

COPY build/libs/*.jar ./

ENV LOG_FORMAT="logstash"

RUN mkdir -p /home/user/.java/.systemPrefs && \
    mkdir -p /home/user/.java/.userPrefs && \
    chmod -R 755 /home/user/.java

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/oom-dump.hprof \
               -Djava.util.prefs.systemRoot=/home/user/.java \
               -Djava.util.prefs.userRoot=/home/user/.java/.userPrefs"

RUN mkdir -p /init-scripts
RUN echo 'java -XX:MaxRAMPercentage=75 -XX:+PrintFlagsFinal -version | grep -Ei "maxheapsize|maxram"' > /init-scripts/0-dump-memory-config.sh