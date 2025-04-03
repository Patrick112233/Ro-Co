keytoolPath=${1:-"/usr/lib/jvm/java-21-openjdk-amd64/bin/keytool"}
"$keytoolPath" -genkey -alias RoCoTLS -storetype PKCS12 -keyalg RSA -keysize 4096 -keystore RoCoTLS.p12 -validity 3650
keytool -export -keystore RoCoTLS.p12 -alias springboot -file RoCoTLS.crt

cp "$(dirname "$keytoolPath")/RoCoTLS.p12" ./RoCoTLS.p12
cp "$(dirname "$keytoolPath")/RoCoTLS.crt" ./RoCoTLS.crt

#https://www.thomasvitale.com/https-spring-boot-ssl-certificate/