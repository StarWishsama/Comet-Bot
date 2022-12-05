FROM azul/zulu-openjdk:17-latest
WORKDIR /usr/src
COPY . .
RUN ./gradlew buildComet && mv ./comet /usr/comet && java -XX:+OptimizeStringConcat -XX:+UseStringDeduplication -jar comet-console.jar
