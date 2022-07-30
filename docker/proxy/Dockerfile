FROM openjdk:8-jdk

WORKDIR /work

COPY build/libs/Netty_NOOB-1.0-SNAPSHOT.jar .
COPY docker/proxy/config.yaml .

RUN touch start.sh
RUN echo "java -jar Netty_NOOB-1.0-SNAPSHOT.jar" >> start.sh

CMD bash start.sh