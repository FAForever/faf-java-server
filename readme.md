# Spring Boot based FAF-Server

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/18f658351dba40c98225f0c9e55d4b82)](https://www.codacy.com/app/FAForever/faf-java-server?utm_source=github.com&utm_medium=referral&utm_content=FAForever/faf-java-server&utm_campaign=badger) [![Build Status](https://travis-ci.org/FAForever/faf-java-server.svg?branch=develop)](https://travis-ci.org/FAForever/faf-java-server) [![Coveralls Status](https://img.shields.io/coveralls/FAForever/faf-java-server/develop.svg)](https://coveralls.io/github/FAForever/faf-java-server)
 
This is a reimplementation of the  [Forged Alliance Forever](https://www.faforever.com/)'s server application.

It abstracts the communication protocol as far as possible in order to stay compatible with current server's
legacy protocol while at the same time allowing new protocols to be added and supported simultaneously.

It's compatible with the legacy database as well, so it can be used as a drop-in replacement of the existing server.
 
## How to run

### Prerequisites

In order to run this software, you need to set up a [FAF database](https://github.com/FAForever/db).

### From source within IntelliJ

1. Clone the repository
1. Import the project into IntelliJ as Gradle project. Doing so will delete the .idea folder which also deletes launch
configurations and code style settings. Please revert such deleted files first (Version Control (Alt+F9) -> Local 
Changes)
1. Configure your JDK 10 if you haven't already
1. Make sure you have the _IntelliJ Lombok plugin_ installed
1. Launch `FafServerApplication`

### From source using the command line

1. Clone the repository
1. Run `.\gradlew bootRun`
 
### From binary

Given the number of required configuration values, it's easiest to run the server using `faf-stack`:

    docker-compose up -d faf-server

## Technology Stack

This project uses:

* Java 10 as the programming language
* [Spring Boot](https://projects.spring.io/spring-boot/) as a base framework
* [Spring Integration](https://projects.spring.io/spring-integration/) as a messaging framework
* [Gradle](https://gradle.org/) as a build automation tool
* [Docker](https://www.docker.com/) to deploy and run the application

## Architecture

[Architecture overview](https://www.draw.io/?lightbox=1&highlight=0000ff&edit=_blank&layers=1&nav=1&title=faf-server-eip-v3.xml#Uhttps%3A%2F%2Fdrive.google.com%2Fuc%3Fid%3D0B9_8tdXfnFw2MnJjZlJ0RDZlMGc%26export%3Ddownload)

## Learn

Learn about Spring Integration: https://www.youtube.com/watch?v=icIosLjHu3I&list=PLr2Nvl0YJxI5-QasO8XY5m8Fy34kG-YF2

# Why I made this

I have high expectations on software quality. There are many reasons why the current Python server, even though it has
been refactored over months, still is of bad quality. So let's compare this Java based server to the current Python
based server.

| Java Server | Python Server |
|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Has thoroughly been performance-tested, proofing that it can withstand more load than FAF will ever experience. | Has never been performance-tested during its development, later performance tests revealed that it handles load very badly and even crashes. |
| Uses a very popular framework in order to: <p><ul><li>Reuse mature and stable technology instead of building "our own thing"</li><li>Reduce the amount of code to be written</li><li>Reduce possible of bugs and vulnerabilities</li><li>Be easily adjustable for future requirements</li></ul></p> | Doesn't use any framework, so that it: <p><ul><li>Reinvents the wheel</li><li>Requires much more code to be written</li><li>Is more error-prone</li><li>Doesn't adjust well for future requirements</li></ul></p> |
| Uses a statically typed programming language which: <p><ul><li>Drastically reduces the amount of possible bugs</li><li>Allows to find many bugs before the software is even started</li><li>Makes it a lot easier for developers to find their way around</li></ul></p> | Uses a dynamically typed programming language which: <p><ul><li>Allows the developer to [do things that make no sense at all](https://semitwist.com/articles/article/view/why-i-hate-python-or-any-dynamic-language-really)</li><li>Makes it very difficult to impossible to automatically detect bugs, so you often only find them when the software is running (even very trivial ones [like typos](https://github.com/FAForever/client/issues/630)). Which caused production problems [more than once](https://github.com/FAForever/server/commit/cdf44be67c1e56368705728e2b70ff6db5f72fe6).</li><li>Makes it much harder for (especially new) developers to find their way around</li></ul></p> |
| Makes use of a very strong and mature ecosystem when it comes to libraries, documentation, build tools, developer tools and everything you need. | Lives in an ecosystem where you often encounter immature libraries and frameworks and/or lack of good documentation. |
| Is highly decoupled from the underlying communication protocol, so that additional protocols can be added easily and supported simultaneously. | Is strongly coupled to the underlying communication protocol so that it's difficult to add new protocols or support multiple protocols at once. Also the server's internal implementation can't be changed easily without breaking client compatibility. |
| Makes heavy use of design patterns of modern Software development, making it easily understandable (for educated developers) and provides high flexibility while avoiding common mistakes. | Makes little use of design patterns, making it more difficult to understand, less flexible and more error-prone. |
| Runs on Windows, Linux and Mac with very little setup time so that every developer can get started within couple of minutes. | Only runs on Linux based systems. In order to run it on Windows, developers have to spend a significant amount of time setting up a Linux virtual machine and installing many dependencies manually. By using Docker some problems are solved but replaced by new ones. |
| Was built from scratch so it's free from any legacy bugs that are unknown or difficult to figure out. | Is a refactoring of a terrible code base so that there are many "left overs", and has given us ridiculous bugs nobody has yet figured out why they happen (see list of problems below). |
| Can be managed while it is running (e.g. configuration values can be changed, or commands can be executed) using a web interface. | Can not be managed once it's running, instead it needs to be restarted for every configuration change. |
| Produces very clear, well readable and helpful log messages making it easy for administrators to analyse and locate problems. | Produces a lot of unreadable, messy and useless log messages, making it difficult for administrators to analyse and locate problems. |
| Was built in accordance with a set of design and implementation principles, like [Clean Code](https://dzone.com/articles/clean-code-principles?edition=154263&utm_source=Weekly%20Digest&utm_medium=email&utm_campaign=wd%202017-01-11), and strong quality control to assure high quality and maintainability. | Was built by "[Hackers](https://danielmiessler.com/study/programmer_hacker_developer/#gs.vEWu9K4)" with little principles or bigger picture in mind, focusing on "getting the job done". |
| Is actively maintained by me, and various other people are (interested in) contributing. | Hasn't had a committed maintainer for over a year, and nobody is willing take over. Many unfinished PRs are lying around as the original authors lost interest, and nobody is taking care. |
| Uses a database abstraction technology so that the application is decoupled from the underlying database, which allows easy switching to a different database. | Is tightly coupled to the rather infamous MySQL database, making it expensive to ever switch to different (better) database. |
| Verifies incoming messages thoroughly. If a messages is invalid in the current context (e. g. a player reports a result for a game he isn't part of, or for an army that doesn't exist) he receives a very clear error message. This protects the server from malicious messages and helps client developers know when their application is misbehaving. | Does little message verification, allowing players to manipulate the system. Silently drops invalid messages or disconnects the client without specifying a reason, making it difficult for client developers to identify mistakes. |

## Solved problems of the current server

The following issues that exist in the original server are not present in this implementation. Even though some of them have only "recently" been reported, they have all been around for at least 1.5 years.

* [FAForever/server#116](https://github.com/FAForever/server/issues/116) Not all online players are reported
* [FAForever/server#142](https://github.com/FAForever/server/issues/142) Server misreporting gameInfo to clients
* [FAForever/server#166](https://github.com/FAForever/server/issues/166) Server bothers with whitespace in json messages
* [FAForever/server#193](https://github.com/FAForever/server/issues/193) Explicit dependency versions
* [FAForever/server#195](https://github.com/FAForever/server/issues/195) Coop Leaderboards [not updating]
* [FAForever/server#200](https://github.com/FAForever/server/issues/200) Ingame swords still display in aeolus
* [FAForever/server#213](https://github.com/FAForever/server/issues/213) Games without game result will still count as rated
* [FAForever/server#224](https://github.com/FAForever/server/issues/224) Player reported as "in game" even though they're not
* [FAForever/server#225](https://github.com/FAForever/server/issues/225) Players in lobby are not always detected properly
* [FAForever/server#253](https://github.com/FAForever/server/issues/253) Player stats sometimes not stored
* [FAForever/server#263](https://github.com/FAForever/server/issues/263) Game duration isn't stored correctly (too long)
* [FAForever/server#265](https://github.com/FAForever/server/issues/265) Rating is always calculated based on player rating when game started
* [FAForever/server#276](https://github.com/FAForever/server/issues/276) 5-15 Zombie-games per day since server (re)start
* [FAForever/server#282](https://github.com/FAForever/server/issues/282) Game titles seem to be quoted needlessly
* [FAForever/server#283](https://github.com/FAForever/server/issues/283) Wrong game result
* [FAForever/server#286](https://github.com/FAForever/server/issues/286) Auto-remove inactives from leaderboards
* [FAForever/server#287](https://github.com/FAForever/server/issues/287) 'Host has left the game' when trying to join a game
* [FAForever/server#288](https://github.com/FAForever/server/issues/288) Explicitly set startTime
* [FAForever/server#302](https://github.com/FAForever/server/issues/302) Unique id handling code does not handle malformed UIDs very well
* [FAForever/server#316](https://github.com/FAForever/server/issues/316) Game stats and player ratings aren't updated before last player disconnects
* [FAForever/server#319](https://github.com/FAForever/server/issues/319) Make ladder map selection non-random
* [FAForever/server#323](https://github.com/FAForever/server/issues/323) Notice from server prompt spacing
* [FAForever/server#343](https://github.com/FAForever/server/issues/339) Teamkills with reference to non-existing games
* [FAForever/server#343](https://github.com/FAForever/server/issues/343) If teams are unlocked the game should not be valid


## Additional features

This implementation provides the following additional features over the original server:

* Management and monitoring using a web interface, allowing for better live-administration
* Automatic update of the GeoIP file (used to display the country flags in the client), eliminating manual updates and
out-of-date/missing flags
* Support for min/max rating for games
* Verification that it's compatible with the underlying database schema version, preventing runtime-errors
* Updating scores for the league & divisions system after each ladder game
* ICE support, improving player connectivity
* Error codes for error messages, allowing easy problem identification, error message translation and specific error
handling client-side
* Connection via WebSocket, allowing websites (and any other tool) to easily connect
* Extended (v2) protocol, fixing shortcomings of the legacy protocol
* Auto-generated v2 protocol documentation to ensure it's always up-to-date, complete and error-free
* OAuth2 support, so that basically everyone can write services that connect to the server
* Helpful, detailed, and runtime-configurable logging for easier investigation in case of problems 
* Authentication toggle, allowing to disable authentication when being run as a test-server
* Transactional database access, ensuring database integrity
* Invalidation of "unfinished" games in case of server crash/restart
* Better protection against cheaters
* Providing player time zones to the client
