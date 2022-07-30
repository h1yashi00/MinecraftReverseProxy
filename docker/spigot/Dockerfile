FROM openjdk:8-jdk

WORKDIR /work
COPY docker/spigot/server.jar .
COPY docker/spigot/eula.txt .
COPY docker/spigot/spigot.yml .
COPY docker/spigot/server.properties .

RUN touch start.sh
RUN echo "java -jar server.jar" >> start.sh

CMD bash start.sh