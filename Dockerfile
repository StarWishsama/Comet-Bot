FROM azul/zulu-openjdk-alpine:17-latest as builder
WORKDIR /usr/src/comet
COPY . .
RUN apt update && apt install -y git && ./gradlew buildComet

FROM azul/zulu-openjdk:17-latest
COPY --from=builder /usr/src/comet/comet /usr/local/comet
CMD ["java", "-XX:+OptimizeStringConcat", "-XX:+UseStringDeduplication", "-jar", "/usr/local/comet/comet-console.jar"]
