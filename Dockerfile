FROM azul/zulu-openjdk:17-latest
WORKDIR /usr/src
COPY . .
RUN apt update && apt install -y git && ./gradlew buildComet && mkdir /usr/comet && mv ./comet /usr/comet && rm -rf /usr/src && cd /usr/comet
CMD ["java", "-XX:+OptimizeStringConcat", "-XX:+UseStringDeduplication", "-jar", "comet-console.jar"]
