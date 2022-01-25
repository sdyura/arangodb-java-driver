#!/bin/bash
set -e

ADB_HOSTS=${ADB_HOSTS:=172.17.0.1:8529}
ADB_ACQUIRE_HOST_LIST=${ADB_ACQUIRE_HOST_LIST:="true"}
ADB_PASSWORD=${ADB_PASSWORD:="test"}

sed -i "/arangodb.hosts/c\arangodb.hosts=$ADB_HOSTS" src/test/resources/arangodb.properties
sed -i "/arangodb.acquireHostList/c\arangodb.acquireHostList=$ADB_ACQUIRE_HOST_LIST" src/test/resources/arangodb.properties
sed -i "/arangodb.password/c\arangodb.password=$ADB_PASSWORD" src/test/resources/arangodb.properties

echo "--------------------"
echo "arangodb.properties:"
echo "--------------------"
cat src/test/resources/arangodb.properties
echo "--------------------"

exec "$@"
