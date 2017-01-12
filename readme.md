# Spring Boot based FAF-Server
 
This is a reimplementation of the  [Forged Alliance Forever](https://www.faforever.com/)'s server application.

It aims to abstract the communication protocol as far as possible in order to stay compatible with current server's
legacy protocol while at the same time allowing new protocols to be added and supported simultaneously.

As the underlying (legacy) database schema isn't based on best education and diligence either, some data types,
structures, concepts and/or field names may be questionable, too, even though efforts are made to abstract it with a
clean layer as well (work in progress)
 
## How to run

### Prerequisites

In order to run this software, you need to set up a [FAF database](https://github.com/FAForever/db).

### From source

In order to run the application from source code:

1. Clone the repository
1. Import the project into IntelliJ
1. Configure your JDK 8 if you haven't already
1. Make sure you have the _IntelliJ Lombok plugin_ installed
1. Launch `FafServerApplication`
 
### From binary
 
In order to run the application from prebuilt binaries:
 
```
docker run --name faf-server \
  -e DATABASE_ADDRESS=localhost:3306 \
  -e DATABASE_USERNAME=root \
  -e DATABASE_PASSWORD=banana \
  -e DATABASE_NAME=faf_lobby \
  -e API_OAUTH2_CLIENT_ID=faf-server \
  -e API_OAUTH2_CLIENT_SECRET=banana \
  -e SERVER_PROFILE=dev \
  -d micheljung/faf-server:latest
```

To run in production, you probably want to create an environment file (e.g. `env.list`):

```
DATABASE_ADDRESS=<db_address>
DATABASE_USERNAME=<username>
DATABASE_PASSWORD=<password>
DATABASE_NAME=<db_name>
API_OAUTH2_CLIENT_ID=<client_id>
API_OAUTH2_CLIENT_SECRET=<client_secret>
SERVER_PROFILE=prod
```

And run with:
```
docker run --name faf-server \
  --env-file ./env.list \
  -d micheljung/faf-server:latest
```

## Technology Stack

This project uses:

* Java 8 as the programming language
* [Spring Boot](https://projects.spring.io/spring-boot/) as a base framework
* [Spring Integration](https://projects.spring.io/spring-integration/) as a messaging framework
* [Gradle](https://gradle.org/) as a build automation tool
* [Docker](https://www.docker.com/) to deploy and run the application

## Architecture

Open with [draw.io](https://www.draw.io/): https://drive.google.com/file/d/0B9_8tdXfnFw2MnJjZlJ0RDZlMGc/view?usp=sharing 

## Learn

Learn about Spring Integration: https://www.youtube.com/watch?v=icIosLjHu3I&list=PLr2Nvl0YJxI5-QasO8XY5m8Fy34kG-YF2

