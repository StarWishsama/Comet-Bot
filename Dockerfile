FROM azul/zulu-openjdk
MAINTAINER StarWishsama
WORKDIR /comet
RUN chmod +x gradlew && gradlew buildComet
# Comet Server Port
EXPOSE 1145
COPY /comet/comet/* /comet/

ENTRYPOINT ["java", "-jar", "/comet-console.jar"]
