FROM azul/zulu-openjdk-alpine:17-latest as builder
WORKDIR /usr/src/comet
COPY . .
RUN apt update && apt install -y git && ./gradlew clean buildComet

FROM azul/zulu-openjdk-alpine:17-latest
COPY --from=builder /usr/src/comet/comet /usr/local/comet
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone  \
    && apk add --no-cache \
       --update \
       curl \
       tar \
       bzip2 \
       freetype \
       mesa-gl \
       tzdata
WORKDIR /usr/local/comet/
CMD ["java", "-XX:+OptimizeStringConcat", "-XX:+UseStringDeduplication", "-Dcomet.no-terminal=true", "-Dfile.encoding=UTF-8", "-jar", "comet-console.jar"]
