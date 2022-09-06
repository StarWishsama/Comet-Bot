FROM azul/zulu-openjdk
MAINTAINER StarWishsama
COPY ./comet /comet
WORKDIR /comet
# Comet Server Port
EXPOSE 1145

ENTRYPOINT ["java", "-jar", "comet-console.jar"]
