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
