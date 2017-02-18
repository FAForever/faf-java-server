# Spring Boot based FAF-Server
 
This is a reimplementation of the  [Forged Alliance Forever](https://www.faforever.com/)'s server application.

It aims to abstract the communication protocol as far as possible in order to stay compatible with current server's
legacy protocol while at the same time allowing new protocols to be added and supported simultaneously.

As the underlying (legacy) database schema isn't based on best education and diligence either, some data types,
structures, concepts and/or field names may be questionable, too, even though efforts are made to abstract it with a
clean layer as well (work in progress)

master|develop
:------------: | :-------------:
[![Build Status](https://travis-ci.org/micheljung/faf-java-server.svg?branch=master)](https://travis-ci.org/micheljung/faf-java-server) | [![Build Status](https://travis-ci.org/micheljung/faf-java-server.svg?branch=develop)](https://travis-ci.org/micheljung/faf-java-server)
[![Coveralls Status](https://img.shields.io/coveralls/micheljung/faf-java-server/master.svg)](https://coveralls.io/github/micheljung/faf-java-server) | [![Coveralls Status](https://img.shields.io/coveralls/micheljung/faf-java-server/develop.svg)](https://coveralls.io/github/micheljung/faf-java-server)
 
## How to run

### Prerequisites

In order to run this software, you need to set up a [FAF database](https://github.com/FAForever/db).

### From source

In order to run the application from source code:

1. Clone the repository
1. Import the project into IntelliJ. For some reason, IntelliJ deletes launch configurations after import. Please revert such deleted files first (Version Control (Alt+F9) -> Local Changes)
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
  -d micheljung/faf-server:0.0.1-SNAPSHOT
```

To run in production, you probably want to create an environment file (e.g. `env.list`):

```
DATABASE_ADDRESS=<db_address>
DATABASE_USERNAME=<username>
DATABASE_PASSWORD=<password>
DATABASE_NAME=<db_name>
API_OAUTH2_CLIENT_ID=<client_id>
API_OAUTH2_CLIENT_SECRET=<client_secret>
UID_PRIVATE_KEY=<private_key>
SERVER_PROFILE=prod
```

And run with:
```
docker run --name faf-server \
  --env-file ./env.list \
  -d micheljung/faf-server:0.0.1-SNAPSHOT
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

# Why I made this

I have high expectations on software quality. There are many reasons why the current Python server, even though it has
been refactored over months, still is of bad quality. So let's compare this Java based server to the current Python
based server.

| Java Server | Python Server |
|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Uses a very popular framework in order to: <p><ul><li>Reuse mature and stable technology instead of building "our own thing"</li><li>Reduce the amount of code to be written</li><li>Reduce possible of bugs and vulnerabilities</li><li>Be easily adjustable for future requirements</li></ul></p> | Doesn't use any framework, so that it: <p><ul><li>Reinvents the wheel</li><li>Requires much more code to be written</li><li>Is more error-prone</li><li>Doesn't adjust well for future requirements</li></ul></p> |
| Uses a statically typed programming language which: <p><ul><li>Drastically reduces the amount of possible bugs</li><li>Allows to find many bugs before the software is even started</li><li>Makes it a lot easier for developers to find their way around</li></ul></p> | Uses a dynamically typed programming language which: <p><ul><li>Allows the developer to [do things that make no sense at all](https://semitwist.com/articles/article/view/why-i-hate-python-or-any-dynamic-language-really)</li><li>Makes it very difficult to impossible to automatically detect bugs, so you usually only find them when the software is running</li><li>Makes it much harder for developers to find their way around<li></ul></p> |
| Makes use of a very strong and mature ecosystem when it comes to libraries, documentation, build tools, developer tools and anything you need. | Lives in an ecosystem that is still quite immature, lacks good documentation and has [laughable build tools](https://pythonrants.wordpress.com/2013/12/06/why-i-hate-virtualenv-and-pip/). |
| Is highly decoupled from the underlying communication protocol, so that additional protocols can be added easily and supported simultaneously. | Is strongly coupled to the underlying communication protocol so that it's difficult to add new protocols or support multiple protocols at once. Also the server's internal implementation can't be changed easily without breaking client compatibility. |
| Makes use of many design patterns of modern Software development, making it easily understandable (for educated developers) and provides high flexibility | Uses almost no design patterns, making it more difficult to understand and less flexible. |
| Runs on Windows, Linux and Mac | Can only be run on Linux based systems (In order to run it on Windows, developers have to set up a Linux virtual machine) |
| Requires very little set up for developers to get started | Requires quite some work for developers to get started, especially on Windows systems (you have to set up a virtual machine) |
| Was built from scratch so it's free from any "legacy bugs" that are unknown or difficult to figure out. | Is a refactoring of a terrible code base so that there are many "left overs", and has given us bugs nobody has yet figured out why they happen. |
| Can be managed while it is running (e.g. configuration values can be changed, or commands can be executed) using a web interface. | Can not be managed once it's running, but needs to be restarted every time. |
| Produces very clear, well readable and helpful logs. | Produces a lot of unreadable, messy or even useless log messages. |
| Follows [Clean Code](https://dzone.com/articles/clean-code-principles?edition=154263&utm_source=Weekly%20Digest&utm_medium=email&utm_campaign=wd%202017-01-11) principles to assure high quality and maintainability. | Was built by "[Hackers](https://danielmiessler.com/study/programmer_hacker_developer/#gs.vEWu9K4)" with no greater design in mind, with focus on "getting the job done". |
| Is actively maintained by me, and soon some others. | Hasn't had a committed maintainer for over a year. |

## Solved problems of the current server

The following issues that exist in the Python server are not present in this Java server:

* [FAForever/server#115](https://github.com/FAForever/server/issues/115) Include session details in message logging for {Lobby,Game}Connection
* [FAForever/server#116](https://github.com/FAForever/server/issues/116) Not all online players are reported
* [FAForever/server#142](https://github.com/FAForever/server/issues/142) Server misreporting gameInfo to clients
* [FAForever/server#193](https://github.com/FAForever/server/issues/193) Explicit dependency versions
* [FAForever/server#195](https://github.com/FAForever/server/issues/195) Coop Leaderboards
* [FAForever/server#213](https://github.com/FAForever/server/issues/213) Coop Leaderboards
* [FAForever/server#224](https://github.com/FAForever/server/issues/224) Player reported as "in game" even though they're not
* [FAForever/server#225](https://github.com/FAForever/server/issues/225) Players in lobby are not always detected properly
* [FAForever/server#253](https://github.com/FAForever/server/issues/253) Player stats sometimes not stored

## Unimplemented features

The following features of the Python server have not yet been implemented in this Java server:

* Updating achievements
* Accepting "mutually agreed draw"
* Timeout unstarted games
* Keep the user's IRC password in sync
* Broadcast message on shutdown
* Tell the client to join chat channels

## Additional features

This Java server provides the following additional features over the Python server:

* Management and monitoring using a web interface
* Automatic, weekly update of the GeoIP file (used to display the country flags in the client)
* Specify min/max rating for games
