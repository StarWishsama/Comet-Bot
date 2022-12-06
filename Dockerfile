FROM azul/zulu-openjdk:17-latest as builder
WORKDIR /usr/src/comet
COPY . .
RUN apt update && apt install -y git && ./gradlew buildComet

FROM azul/zulu-openjdk:17-latest
COPY --from=builder /usr/src/comet/comet /usr/local/comet
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
CMD ["java", "-XX:+OptimizeStringConcat", "-XX:+UseStringDeduplication", "-Dcomet.no-terminal", "-jar", "/usr/local/comet/comet-console.jar"]
