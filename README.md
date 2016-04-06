# COMP3004-Ivanhoe
group project for COMP3004 (Object-Oriented Software Engineering)

### Table of Contents:
1. [Authors](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md#authors)     
2. [About](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md#about)
3. [Major Releases](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md#major-releases)
4. [Installation/Setup](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md#installation--setup)
5. [Running](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md#running)
6. [Design](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md#design)
  - [Networking](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md##networking)
  - [Overall Architecture](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md##overall-architecture)
  - [Patterns](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md##patterns)
  - [Refactoring](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md##refactoring)
  - [Pros/Cons](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md##pros--cons)

---
### Authors:
Team #13: 
- [Matthew Pepers](https://github.com/pepers)
- [Khalil van Alphen](https://github.com/kanguilla)

---
### About:
Ivanhoe is a card game by Reiner Knizia.  The rules and faq for the game can be found at [/doc/rules/](https://github.com/pepers/COMP3004-Ivanhoe/tree/master/doc/rules).  This Java project attempts to faithfully recreate the original game as a digital computer game, in which you can connect to other players over a network.

---
### Major Releases:
- [Iteration 1](https://github.com/pepers/COMP3004-Ivanhoe/tree/v1.0)
- Final Iteration - TBA

---
### Installation / Setup:
- have the project open in Eclipse
- make sure the project is using Java 8 (add it to the build path in preferences)
- navigate to /src/test/resources/
- right-click on the following files, go to 'Build Path' -> 'Add to Build Path':
  - hamcrest-core-1.3.jar
  - junit-4.12.jar
  - log4j-1.2.17.jar
  - miglayout-4.0.jar

---
### Running:
- open a command prompt/terminal
- navigate to Client.jar and Server.jar files 
  - for Iteration 1: [/doc/iteration1/](https://github.com/pepers/COMP3004-Ivanhoe/tree/master/doc/iteration1)
  - for Final Iteration: TBA
- run the Server
  - `java -jar Server.jar`
- run the Client(s)
  - in GUI Mode
    - `java -jar Client.jar`
  - or Command Line Mode
    - `java -jar Client.jar -c`

---
### Design:

#### Networking:

#### Overall Architecture:

#### Patterns:

#### Refactoring:

#### Pros/Cons:

