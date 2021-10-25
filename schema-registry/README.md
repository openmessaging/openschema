# OpenSchema Schema Registry 

## OpenSchema Schema Registry overview

OpenSchema Schema Registry provides support for you to store schema information in a textual format (typically JSON) and access that information whenever applications need it.

The Schema Registry deals with the following elements:

* A subject which is a logical name of the schema

* The schema version which is a specific version of a schema document


OpenSchema Schema Registry provides the following components

* Standalone Schema Registry Server

  ```
  By default, it is using an H2 database, but server can be used with other databases by providing appropriate datasource configuration.
  ```

* Schema Registry API are designed to follow OpenSchema Specification for storing, accessing schema documents.

  ```
  Currently, the API communicates to the standalone schema registry server.
  ```

## Project page

You can read more about OpenSchema by going to [the project page](https://github.com/openmessaging/openschema)
## Building

### Basic Compile and Test

To build the source you will need to install JDK 1.8.

OpenSchema Schema Registry uses Maven for build-related activities, and you
should be able to get off the ground quite quickly by cloning the
project you are interested in and typing

```
$ ./mvn clean install -DskipTests
```

This should result in producing an executable JAR, and in-memory persistence implementation.

```
java -jar target/registry-server-0.0.1-SNAPSHOT.jar
```

This should result in the Schema Registry starting up and the registry available on localhost port 8081

