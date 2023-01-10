FROM azul/zulu-openjdk-alpine:17-latest as builder
WORKDIR /usr/src/comet
COPY . .
RUN apk add --no-cache --update git && ./gradlew clean buildComet

FROM azul/zulu-openjdk:19-latest
COPY --from=builder /usr/src/comet/comet /usr/local/comet
ENV TZ=Asia/Shanghai
RUN apt update && apt install -y tzdata libgl1-mesa-glx && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
WORKDIR /usr/local/comet/
CMD ["java", "-XX:+OptimizeStringConcat", "-XX:+UseStringDeduplication", "-Dcomet.no-terminal=true", "-Dfile.encoding=UTF-8", "-jar", "comet-console.jar"]
