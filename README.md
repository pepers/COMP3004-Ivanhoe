# COMP3004-Ivanhoe
group project for COMP3004 (Object-Oriented Software Engineering)

## Table of Contents:
1. [Authors](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md#authors)     
2. [About](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md#about)
3. [Major Releases](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md#major-releases)
4. [Installation/Setup](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md#installation--setup)
5. [Running](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md#running)
6. [Design](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md#design)
  - [Networking](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md#networking)
  - [Overall Architecture](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md#overall-architecture)
  - [Patterns](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md#patterns)
  - [Refactoring](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md#refactoring)
  - [Pros/Cons](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/README.md#proscons)

---
## Authors:
Team #13: 
- [Matthew Pepers](https://github.com/pepers)
- [Khalil van Alphen](https://github.com/kanguilla)

---
## About:
Ivanhoe is a card game by Reiner Knizia.  The rules and faq for the game can be found at [/doc/rules/](https://github.com/pepers/COMP3004-Ivanhoe/tree/master/doc/rules).  This Java project attempts to faithfully recreate the original game as a digital computer game, in which you can connect to other players over a network.

---
## Major Releases:
- [Iteration 1](https://github.com/pepers/COMP3004-Ivanhoe/tree/v1.0)
- Final Iteration - TBA

---
## Installation / Setup:
- have the project open in Eclipse
- make sure the project is using Java 8 (add it to the build path in preferences)
- navigate to /src/test/resources/
- right-click on the following files, go to 'Build Path' -> 'Add to Build Path':
  - hamcrest-core-1.3.jar
  - junit-4.12.jar
  - log4j-1.2.17.jar
  - miglayout-4.0.jar

---
## Running:
- open a command prompt/terminal
- navigate to Client.jar and Server.jar files 
  - for Iteration 1: [/doc/iteration1/](https://github.com/pepers/COMP3004-Ivanhoe/tree/master/doc/iteration1)
  - for Final Iteration: TBA
- run the Server
  - normal game
    - `java -jar Server.jar`
  - real-time Ivanhoe
    - `java -jar Server.jar -r`
- run the Client(s)
  - in GUI Mode
    - `java -jar Client.jar`
  - or Command Line Mode
    - `java -jar Client.jar -c`

---
## Design:
#### Networking:
Our strategy for networking was to pass lots of serializable objects between the Clients and Server.  When the Client is started, a new thread is created for reading user input, another thread for receiving objects from the Server over the socket.  When the Server is started, a thread is created for reading user input from the console, another thread is created just to wait for new Clients that are attempting to connect, and a final thread to handle the Server's responsibilities to the game.  On both the Client and Server, everything that is receieved is a serializable object, including all Client Actions, Chat messages, and Player and GameState objects, etc.

#### Overall Architecture:

#### Patterns:
  1. Chain-of-Responsibility:
    * ValidCommand class ([ValidCommand UML Class Diagram](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/doc/uml/class%20diagrams/CD-ValidCommand.png)):
      - the Client and Server each have a list of commands that the user can type in, in Command Line (CLI) Mode
      - each command starts with `/` and a list of commands and their syntax can be viewed by typing `/help`
      - the commands are required to play through the game, with such favourites as `/play [card]` and `/withdraw`
      - it was quickly noticed that when checking for the validity of typed commands (such as their arguments, and whether it was an appropriate time to use that command or not), there were a lot of repetition in what we were checking for, and often many checks would need to happen in a row
      - the Chain-of-Responsibility design pattern allowed us to chain multiple validity checks together, reuse those checks for multiple commands, and return quickly if any one check failed

  2. Command:
    * PromptCommand concrete class, CommandInterface, and CommandInvoker class ([ExecuteActionCards UML Class Diagram](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/doc/uml/class%20diagrams/CD-ExecuteActionCards.png)):
      - the Server has a prompt(String,Player,ArrayList) method that prompts a Client, corresponding to a Player, for a choice from the ArrayList
      - this is used to get the player's choice for things such as which token they want after winning a purple tournament, but also for Action card choices when they attempt to play an Action card
      - we realized that it would be a good design choice to encapsulate the rules of the game involving Action cards within the GameState class, but the GameState does not have knowledge of the Server or the Client (which we believe it shouldn't)
      - but we still needed to prompt the Client for choices involving Action cards while they're rules were being executed in the GameState
      - the Command design pattern allowed us to invoke a concrete PromptCommand class to run the prompt method on the Server, from within the execute method in the GameState
    * ClientAI ([corresponding UML Class Diagram](https://github.com/pepers/COMP3004-Ivanhoe/blob/master/doc/uml/class%20diagrams/CD-AI.png)):
      - the ClientAI class runs a Client through AI decisions
      - multiple AI can play against each other or against human players
      - ClientAI makes use of the Command design pattern by executing the concrete classes: StartTournament, PlayCard, EndTurn, and Withdraw
      - each time the AI invokes the concrete classes's methods, it makes a decision to take action or not (based on the AI's skill levels)
      - this was an excellent design decision and allowed us to encapsulate all actions and information needed for the AI to make each of it's four major decisions

  3. Strategy:
  
  4. Facade:


#### Refactoring:
Since iteration 1, the following has been refactored:
- Colour class added:
  - many classes had a colour, such as DisplayCards, Tournament, Token, etc.
  - that duplicate code was encapsulated in the new Colour class 
- ValidCommand class added, and Chain-of-Responsibility pattern:
  - the list of commands that could be used on the Client and Server grew quite a bit
  - but the Client and Server verified the commands slightly differently
  - ValidCommand was added to standardize the checks for valid commands across Client and Server, and removed some duplicate code
- Executing Action Cards:
  - rules for Action card execution were checked on Client and Server and GameState at first
  - most of the execution rules were then brought to the GameState class, so that the rules would be separate, and would only need to be checked in one place
- GUI and AI were introduced
- Prompt objects:
  - were added to send the Client their choices
  - removed a lot of code were the Client originally made decisions that were better left to the GameState or Server

#### Pros/Cons:

