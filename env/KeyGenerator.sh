# private key
openssl ecparam -genkey -name secp521r1 -noout -out ecdsa-p521-private.pem
# public key
openssl ec -in ecdsa-p521-private.pem -pubout -out ecdsa-p521-public.pem

# PCKs(
openssl pkcs8 -topk8 -inform pem -in ecdsa-p521-private.pem -outform pem -nocrypt -out private_pkcs8.pem

# cert
openssl req -new -x509 -key ecdsa-p521-private.pem -out ecdsa-p521-cert.crt -days 24854

