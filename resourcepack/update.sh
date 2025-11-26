#!/usr/bin/env bash

cd ./data
rm ./../data.zip
zip ./../data.zip -r .
HASH=$(sha1sum ./../data.zip | awk '{print $1}')

# load .env
set -a
source ./../.env
set +a

curl --insecure -T ./../data.zip -u "$DATA_USER:$DATA_PASS" "$DATA_HOST$DATA_PATH"

curl --insecure -u "$SERVER_USER:$SERVER_PASS" "$SERVER_HOST$SERVER_PATH" -o "./../server.properties"
sed -i -E "s|^resource-pack-sha1=.*$|resource-pack-sha1=$HASH|" ./../server.properties
curl --insecure -T ./../server.properties -u "$SERVER_USER:$SERVER_PASS" "$SERVER_HOST$SERVER_PATH"

sed -i -E "s|^resource-pack-sha1=.*$|resource-pack-sha1=$HASH|" "$LOCAL_PATH/server.properties"