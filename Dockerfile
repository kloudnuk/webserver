FROM maven:3.6.3-openjdk-17-slim as BUILDER
ARG VERSION=0.0.1-SNAPSHOT
WORKDIR /build/
COPY pom.xml /build/
COPY src /build/src/

RUN mvn clean package -Dmaven.test.skip=true
COPY target/kloudnuk-${VERSION}.jar target/application.jar

FROM ubuntu
SHELL ["/bin/bash", "-c"]

RUN apt update && \
    apt upgrade -y && \
    apt install -y software-properties-common jq wget curl openjdk-17-jdk openjdk-17-jre

ENV KN_PASSGEN="$(echo $RANDOM | md5sum | head -c 14)"
EXPOSE 8000
VOLUME [ "/nuk" ]
COPY --from=BUILDER /build/target/application.jar /app/
ENTRYPOINT [ "java", "-jar", "/app/application.jar", "--debug" ]
