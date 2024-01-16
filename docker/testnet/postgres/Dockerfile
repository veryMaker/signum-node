# Build the node software
FROM gradle:7.3.3-jdk11 as builder
WORKDIR /signum
COPY . .
RUN gradle dist --no-daemon


# Prepare final image
FROM openjdk:11-jre-slim
RUN apt-get update && apt-get install -y bash curl wget unzip git
LABEL name="Signum Node Postgres [Testnet]"
LABEL description="This is the official Signum Node using MariaDB database"
WORKDIR /signum
COPY --from=builder /signum/build/distributions .
RUN unzip -o signum-node.zip
RUN rm -rf conf

VOLUME ["/conf"]
RUN ln -s /conf /signum/conf

# We use the bootstrap folder to copy the config files to the host machine in the start-node.sh script
COPY conf/logging-default.properties /signum/bootstrap/logging-default.properties
COPY conf/node-default.properties /signum/bootstrap/node-default.properties
COPY conf/node.properties.postgres /signum/bootstrap/node.properties

# Update phoenix and classic wallets
COPY  update-phoenix.sh /signum/update-phoenix.sh
COPY  update-classic.sh /signum/update-classic.sh
RUN chmod +x update-phoenix.sh
RUN chmod +x update-classic.sh
RUN bash -c /signum/update-phoenix.sh
RUN bash -c /signum/update-classic.sh

COPY docker/scripts/start-node.sh /signum/start-node.sh
RUN chmod +x start-node.sh

# Clean up
RUN rm signum-node.exe 2> /dev/null || true
RUN rm signum-node.zip 2> /dev/null || true
RUN rm -rf tmp 2> /dev/null || true

EXPOSE 6877 6876 7123
ENTRYPOINT [ "./start-node.sh" ]
