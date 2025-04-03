# private key
openssl ecparam -genkey -name secp521r1 -noout -out ecdsa-p521-private.pem
# public key
openssl ec -in ecdsa-p521-private.pem -pubout -out ecdsa-p521-public.pem

# PCKs(
openssl pkcs8 -topk8 -inform pem -in ecdsa-p521-private.pem -outform pem -nocrypt -out private_pkcs8.pem
#openssl ec -pubin -inform PEM -in ecdsa-p521-public.pem -pubout -outform PEM -out publickey-pkcs8.pem

# cert
openssl req -new -key ecdsa-p521-private.pem -out request.csr
openssl req -x509 -key ecdsa-p521-private.pem -in request.csr -out certificate_x509.pem -days 365
#openssl req -new -x509 -key ecdsa-p521-private.pem -out ecdsa-p521-cert.crt -days 24854

