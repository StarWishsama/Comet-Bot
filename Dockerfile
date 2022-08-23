FROM azul/zulu-openjdk-debian:17-latest
MAINTAINER StarWishsama
WORKDIR /comet
RUN set -eux; \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime; \
    echo $TZ > /etc/timezone \
    && chmod +x gradlew \
    && gradlew buildComet
# Comet Server Port
EXPOSE 1145
COPY /comet/comet/* /comet/

ENTRYPOINT ["java", "-jar", "/comet-console.jar"]
