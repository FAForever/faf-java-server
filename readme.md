# Spring Boot based FAF-Server Prototype
 
This is a prototype of a Spring Boot based server application for Forged Alliance Forever.
 
## How to run from source

1. Clone the repository
1. Import the project into IntelliJ
1. Configure your JDK 8 if you haven't already
1. Make sure you have the _IntelliJ Lombok plugin_ installed
1. Launch `FafServerApplication`
 
## How to run from binary
 
Either check out the source code and execute the run configuration `FafServerApplication`, or run it directly
from the published Docker image like so:
 
```
docker run --name faf-server \
  -e DATABASE_ADDRESS=192.168.99.100:3306 \
  -e DATABASE_USERNAME=root \
  -e DATABASE_PASSWORD=banana \
  -e DATABASE_NAME=faf_lobby \
  -d micheljung/faf-server:latest
```

To run in production, you probably want to create an environment file (e.g. `env.list`):

```
DATABASE_ADDRESS=stable_faf-db_1
DATABASE_USERNAME=faf_lobby
DATABASE_PASSWORD=password
DATABASE_NAME=faf_lobby
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


## Learn

Learn about Spring Integration: https://www.youtube.com/watch?v=icIosLjHu3I&list=PLr2Nvl0YJxI5-QasO8XY5m8Fy34kG-YF2
