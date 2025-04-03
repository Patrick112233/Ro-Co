# RoCO Rest API:


## Quick Start
Run docker-compose up to start the API server.
... 


## Configuration
The API server is configured using environment variables. The following variables are available:

# RoCO Rest API:

## Quick Start
Run docker-compose up to start the API server.
...

## Configuration
### Server Configuration
- **`server.port=443`**: Specifies the port number for secure server communication (HTTPS).
- **`server.http.port=8080`**: Specifies the port number for non-secure server communication (HTTP).

### MongoDB Settings
- **`spring.data.mongodb.port=27017`**: Defines the MongoDB server port.
- **`spring.data.mongodb.username=`**: Sets the username for the MongoDB connection.
- **`spring.data.mongodb.password=`**: Sets the password for the MongoDB connection.  
  *Hint*: Use environment variables to store sensitive data like passwords.

### Security Keys
- **`security.jwt.private-key=YOUR_PRIVATE_KEY`**: PKCS8 private key using ES512 for JWT authentication. Ensure this key is generated and stored securely.
- **`security.jwt.public-key=YOUR_PUBLIC_KEY`**: X.509 public key certificate using ES512 for verifying JWT signatures. Secure storage is essential.

### SSL Configuration
- **`server.ssl.key-store=classpath:certs/RoCoTLS.p12`**: Path to your SSL key store containing certificates for TLS communication.

### Hints for Key Generation

To generate secure keys:

1. **JWT Keys**: Use the `JWTKeyGenerator` script located in the `/env/` directory to generate private and public keys for JWT authentication.
2. **TLS Certificates**: Run the `TLSCertGenerator.sh` script in the `/env/` directory to create TLS certificates and the `.p12` key store file.

By following these steps and securely handling sensitive data, you'll be able to configure the application effectively.

## API Documentation
Die API Endpunkte sind dokumentirt unter folgendem Link: [https://localhost/api.html](https://localhost/api.html).
