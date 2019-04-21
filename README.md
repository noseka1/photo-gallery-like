# photo-gallery-like

## Build

This component requires the `photo-gallery-common` library. Make sure you build that library first.

You can build this project using:

```
mvn clean install package
```
## Database

This component requires access to a PostgreSQL database. You can create it using:

```
psql -c 'CREATE DATABASE likedb'
psql -c "CREATE USER likeuser WITH ENCRYPTED PASSWORD 'password'"
psql -c 'GRANT ALL PRIVILEGES ON DATABASE likedb TO likeuser'
```

## Run

You can run this component as a standalone service using:

```
java -jar target/photo-gallery-like-1.0-SNAPSHOT-runner.jar
```

After the service starts up you can test it using curl.

To add some likes to the photo with ID 2:

```
curl -v -X POST -H 'Content-Type: application/json' --data '{"id":"2","likes":"5"}' localhost:8081/likes
curl -v -X POST -H 'Content-Type: application/json' --data '{"id":"2","likes":"2"}' localhost:8081/likes

```

To retrieve likes received by all photos:

```
curl -v localhost:8081/likes
```

## Deploying to Minishift

Create a new project if it doesn't exist:

```
oc new-project photo-gallery-distributed
```

Deploy a PostgreSQL database:

```
oc new-app \
--template postgresql-persistent \
--param DATABASE_SERVICE_NAME=postgresql-like \
--param POSTGRESQL_USER=likeuser \
--param POSTGRESQL_PASSWORD=password \
--param POSTGRESQL_DATABASE=likedb
```

Prepare to connect to the Docker daemon running within the Minishift virtual machine:

```
eval $(minishift docker-env)
```

Build the application image:

```
docker build \
-f src/main/docker/Dockerfile.jvm \
-t 172.30.1.1:5000/photo-gallery-distributed/like \
.
```

Push the application image into the Minishift's integrated Docker registry:

```
docker login -u `oc whoami` -p `oc whoami -t` 172.30.1.1:5000
docker push 172.30.1.1:5000/photo-gallery-distributed/like
```

Deploy the application:

```
oc new-app \
--image-stream like \
--name like \
--env QUARKUS_DATASOURCE_URL=jdbc:postgresql://postgresql-like:5432/likedb
```

Expose the application to the outside world:

```
oc expose svc like
```
