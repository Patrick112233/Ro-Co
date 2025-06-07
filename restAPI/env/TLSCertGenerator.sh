keytoolPath=$(which keytool)
if [ -z "$keytoolPath" ]; then
  echo "keytool not found in PATH"
  exit 1
fi

# Set default values for non-interactive generation, allow override via environment variables
DNAME="${DNAME:-CN=RoCoTLS, OU=Informatics, O=RoCo, L=Rosenheim, ST=Bavaria, C=DE}"
STOREPASS="${STOREPASS}"
KEYPASS="${KEYPASS}"

"$keytoolPath" -genkeypair \
  -alias RoCoTLS \
  -storetype PKCS12 \
  -keyalg RSA \
  -keysize 4096 \
  -keystore RoCoTLS.p12 \
  -validity 3650 \
  -dname "$DNAME" \
  -storepass "$STOREPASS" \
  -keypass "$KEYPASS"

"$keytoolPath" -exportcert \
  -keystore RoCoTLS.p12 \
  -alias RoCoTLS \
  -file RoCoTLS.crt \
  -storepass "$STOREPASS"

# Files are already in the current directory; no need to copy

#cp RoCoTLS.p12 ./RoCoTLS.p12
#cp RoCoTLS.crt ./RoCoTLS.crt

#https://www.thomasvitale.com/https-spring-boot-ssl-certificate/