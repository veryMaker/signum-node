# Build the node software
#FROM gradle:7.3.3-jdk11 as builder
FROM amazoncorretto:11-alpine-jdk as builder
RUN  apk update && apk upgrade \
  && apk add --no-cache --update coreutils bind-tools git unzip wget curl nodejs npm \
  && rm -rf /var/cache/apk/*

WORKDIR /signum-node

COPY . .

RUN node -v
RUN npm -v

# Build Signum Node Jar
RUN chmod +x /signum-node/gradlew \
  && /signum-node/gradlew clean dist jdeps \
    --no-daemon \
    -Pjdeps.recursive=true \
    -Pjdeps.ignore.missing.deps=true \
    -Pjdeps.print.module.deps=true

# Unpack the build to /signum
RUN unzip -o build/distributions/signum-node.zip -d /signum

# provide needed update scripts
RUN chmod +x update-phoenix.sh
RUN chmod +x update-classic.sh
COPY update-phoenix.sh /signum/update-phoenix.sh
COPY update-classic.sh /signum/update-classic.sh

WORKDIR /signum

# Clean up /signum
RUN rm -rf /signum/signum-node.exe 2> /dev/null || true \
  && rm -rf /signum/signum-node.zip 2> /dev/null || true

## Create a custom JRE
RUN $JAVA_HOME/bin/jlink \
  --add-modules $(cat /signum-node/build/reports/jdeps/print-module-deps-main.txt) \
  --strip-debug \
  --no-man-pages \
  --no-header-files \
  --compress=2 \
  --output /jre

RUN mkdir -p /requirements \
  && ldd /jre/bin/java | awk 'NF == 4 { system("cp --parents " $3 " /requirements") }'

# Prepare final image
FROM alpine:3.18
LABEL name="Signum Node"
LABEL description="This is the official Signum Node image"
LABEL credits="gittrekt,damccull,ohager"
ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# minimum required env needed for wallet update scripts on container restarts -> see start-node.sh
RUN  apk update && apk upgrade \
  && apk add --no-cache --update coreutils bind-tools git unzip wget curl bash \
  && rm -rf /var/cache/apk/*

COPY --from=builder /jre $JAVA_HOME
COPY --from=builder /signum /signum
COPY --from=builder /requirements/ /

WORKDIR /signum

VOLUME ["/conf", "/db"]
RUN ln -s /conf /signum/conf
RUN ln -s /db /signum/db

# We use the bootstrap folder to copy the config files to the host machine in the start-node.sh script
# use one of [h2,mariadb,postgres]
ARG database=h2
ARG network=mainnet

# Injectable ports defaulting to mainnet
ARG port_p2p=8123
ARG port_http=8125
ARG port_ws=8126

RUN mkdir ./bootstrap
COPY conf/logging-default.properties ./bootstrap/logging-default.properties
COPY conf/node-default.properties ./bootstrap/node-default.properties
COPY conf/${network}/node.${database}.properties ./bootstrap/node.properties

COPY docker/scripts/start-node.sh ./start-node.sh
RUN chmod +x start-node.sh

# Clean up
RUN rm signum-node.exe 2> /dev/null || true
RUN rm signum-node.zip 2> /dev/null || true

EXPOSE $port_ws $port_http $port_p2p

ENTRYPOINT [ "./start-node.sh" ]
