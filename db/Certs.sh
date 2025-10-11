#!/usr/bin/env bash
# This script creates a self-signed root certificate, a user certificate, and a database certificate.
# The root certificate signs the user and database certificates, and only PEM files are generated.
# ECC (Elliptic Curve Cryptography) is used

set -euo pipefail

# Always operate relative to this script's directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Create the output folder
OUT_DIR="$SCRIPT_DIR/out"
[ -d "$OUT_DIR" ] && rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

# Set distinguished names
ROOT_SUBJECT="/CN=RoCoRootCA/OU=RoCoCA/O=RoCo/L=Rosenheim/ST=BY/C=GE"
API_SUBJECT="/CN=RoCoAPI/OU=RoCoAPI/O=RoCo/L=Rosenheim/ST=BY/C=GE"
DB_SUBJECT="/CN=RoCoDB/OU=RoCoDB/O=RoCo/L=Rosenheim/ST=BY/C=GE"

#Config DB DNS (Docker service name is default!)
DBDNSNAME="mongo"
DBIPADDRESS="127.0.0.1"

# Define the elliptic curve to use
ECC_CURVE="prime256v1"

# Root certificate (self-signed)
# Generate private key 
openssl ecparam -genkey -name "$ECC_CURVE" -out "$OUT_DIR/RoCoRootCA.key"
# Create certificate
openssl req -x509 -new -nodes -key "$OUT_DIR/RoCoRootCA.key" -sha256 -days 3650 -out "$OUT_DIR/RoCoRootCA.pem" -subj "$ROOT_SUBJECT"

# API certificate / Client (signed by root)
# Generate private key
openssl ecparam -genkey -name "$ECC_CURVE" -out "$OUT_DIR/RoCoAPI.key"
# Certificate signing request (CSR) 
openssl req -new -key "$OUT_DIR/RoCoAPI.key" -out "$OUT_DIR/RoCoAPI.csr" -subj "$API_SUBJECT"
# Extend fields for DB authentication
cat > "$OUT_DIR/api_cert_config.cnf" << EOL
[ v3_req ]
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = clientAuth
EOL
# Sign the API certificate with the root certificate
openssl x509 -req -in "$OUT_DIR/RoCoAPI.csr" -CA "$OUT_DIR/RoCoRootCA.pem" -CAkey "$OUT_DIR/RoCoRootCA.key" -CAcreateserial \
  -out "$OUT_DIR/RoCoAPI_cert.pem" -days 3650 -sha256 -extfile "$OUT_DIR/api_cert_config.cnf" -extensions v3_req
cat "$OUT_DIR/RoCoAPI.key" "$OUT_DIR/RoCoAPI_cert.pem" > "$OUT_DIR/RoCoAPI.pem"

# Database certificate / server (signed by root)
# Generate private key
openssl ecparam -genkey -name "$ECC_CURVE" -out "$OUT_DIR/RoCoDB.key"
#  certificate signing request (CSR)
openssl req -new -key "$OUT_DIR/RoCoDB.key" -out "$OUT_DIR/RoCoDB.csr" -subj "$DB_SUBJECT"

cat > "$OUT_DIR/db_cert_config.cnf" << EOL
[ v3_req ]
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names

[ alt_names ]
DNS.1 = $DBDNSNAME
IP.1 = $DBIPADDRESS
EOL

# Sign the database certificate with the root certificate
openssl x509 -req -in "$OUT_DIR/RoCoDB.csr" -CA "$OUT_DIR/RoCoRootCA.pem" -CAkey "$OUT_DIR/RoCoRootCA.key" -CAcreateserial \
  -out "$OUT_DIR/RoCoDB_cert.pem" -days 3650 -sha256 -extfile "$OUT_DIR/db_cert_config.cnf" -extensions v3_req
cat "$OUT_DIR/RoCoDB.key" "$OUT_DIR/RoCoDB_cert.pem" > "$OUT_DIR/RoCoDB.pem"

