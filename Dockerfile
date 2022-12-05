FROM azul/zulu-openjdk-alpine:17-latest
WORKDIR /usr/src/comet
COPY . .
RUN apt update && apt install -y git && ./gradlew buildComet

FROM ubuntu:latest
COPY --from=builder /usr/src/comet/comet /usr/local/comet
CMD ["java", "-XX:+OptimizeStringConcat", "-XX:+UseStringDeduplication", "-jar", "/usr/local/comet/comet-console.jar"]
