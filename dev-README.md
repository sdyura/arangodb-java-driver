# dev-README

## native image reflection configuration

To generate reflection configuration run `helper.NativeImageHelper` and copy the generated json to `src/main/resources/META-INF/native-image/com.arangodb/arangodb-java-driver/reflect-config.json`.


## test from docker container

Build the image:

```shell
docker build -f tests/docker/Dockerfile -t arangodb-java-driver-tests:latest .
```

Run the container:

```shell
docker run arangodb-java-driver-tests:latest
```

The following environment variables can be set:

- `ADB_HOSTS`: eg. `c1:8529,c2:8529,c3:8529`
- `ADB_ACQUIRE_HOST_LIST`: `(true|false)`
- `ADB_PASSWORD`

