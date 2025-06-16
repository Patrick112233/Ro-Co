# Standalone Execution environment for the RoCo REST API including JWT key and self signed TLS certificate.
# docker build -t restAPI .
# docker run --rm -it -e STOREPASS=123456 -e KEYPASS=123456 -e MONGO_INITDB_ROOT_PASSWORD=zIp3A89zA6YE5psB9YjeB -e MONGO_INITDB_ROOT_USERNAME=backend -e MONGO_INITDB_PORT=27017 restAPI

FROM openjdk:21-jdk-slim

RUN apt-get update && apt-get install -y openssl maven && rm -rf /var/lib/apt/lists/*

RUN mkdir /restAPI
COPY . /restAPI
RUN mkdir /bin/roco
RUN mkdir -p /bin/roco/certs

WORKDIR /restAPI
RUN mvn -B package --file /restAPI/pom.xml -DskipTests -Djacoco.skip=true

RUN cp /restAPI/target/ro-co-0.0.1.jar /bin/roco/ro-co-0.0.1.jar&& \
    cp /restAPI/src/main/resources/application.properties /bin/roco/application.properties

# Modify the application.properties to overwrite the server.ssl.key-store
RUN sed -i '/^server\.ssl\.key-store=/d' /bin/roco/application.properties && \
    echo "server.ssl.key-store=/bin/roco/certs/RoCoTLS.p12" >> /bin/roco/application.properties

RUN cp /restAPI/env/JWTKeyGenerator.sh /bin/roco/certs/JWTKeyGenerator.sh && \
    cp /restAPI/env/TLSCertGenerator.sh /bin/roco/certs/TLSCertGenerator.sh && \
    chmod +x /bin/roco/certs/JWTKeyGenerator.sh /bin/roco/certs/TLSCertGenerator.sh

WORKDIR /bin/roco/certs


CMD ["/bin/bash", "-c", "\
    ./JWTKeyGenerator.sh && \
    ./TLSCertGenerator.sh && \
    export PKCS8_PRIVATE_KEY=\"$(awk 'NR>1 && $0 !~ /-----END/ {printf \"%s\", $0}' private_pkcs8.pem)\" && \
    export X508_PUBLIC_KEY=\"$(awk 'NR>1 && $0 !~ /-----END/ {printf \"%s\", $0}' certificate_x509.pem)\" && \
    echo \"$PKCS8_PRIVATE_KEY\" | base64 -d > /dev/null && echo \"PKCS8_PRIVATE_KEY: Base64 valid\" || echo \"PKCS8_PRIVATE_KEY: Base64 invalid\" && \
    echo \"$X508_PUBLIC_KEY\" | base64 -d > /dev/null && echo \"X508_PUBLIC_KEY: Base64 valid\" || echo \"X508_PUBLIC_KEY: Base64 invalid\" && \
    java -jar /bin/roco/ro-co-0.0.1.jar \
"]