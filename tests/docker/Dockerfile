FROM docker.io/maven:3.8.4-openjdk-17
COPY . /app
WORKDIR /app
RUN mvn test -DskipTests=true
ENTRYPOINT ["/app/tests/docker/entrypoint.sh"]
CMD ["mvn", "surefire:test"]
