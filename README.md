# 🧙‍♂️ Simple Text Adventure Game (STAG)

A server–client text adventure built in Java, demonstrating strong Object-Oriented Design, network programming, and software architecture skills. 

Explore a connected world, collect artefacts, interact with mysterious characters, and uncover a hidden treasure — all through text commands.

---

## Getting Started

### 1️⃣ Start the Game Server
From the project root directory, run:
```bash
./mvnw clean compile exec:java@server
```

### 2️⃣ Launch a Game Client

In a separate terminal, start a player session by providing a username:

```bash
./mvnw clean compile exec:java@client -Dexec.args="user"
```

Each connected client represents a live player in the same persistent world.



## Gameplay

The world is defined using a .dot file (locations, paths, and entities) and an actions.xml file (verbs, interactions, and effects).
Players type natural-language commands to explore, collect, craft, and act.

Basic Commands

Command	Alias	Description
look	—	Describes your current location and nearby entities.
inventory	inv	Lists artefacts you’re currently carrying.
get <item>	—	Pick up an artefact from the current location.
drop <item>	—	Place an artefact from your inventory into the location.
goto <place>	—	Move to a connected location.
health	—	Check your current health status.




Example Session
```
user:> look
You are in cabin - A log cabin in the woods.

You can see artefacts:
  potion - A bottle of magic potion
  axe - A razor sharp axe
  coin - A silver coin

You can see furniture:
  trapdoor - A locked wooden trapdoor in the floor

You can access from here:
  forest
```
```
user:> get potion
user:> drink potion
You drink the potion and your health improves.
```



## World Overview

The game world consists of several interconnected locations, each containing artefacts, furniture, or characters.

Location	Notable Entities	Description
Cabin	Potion, Axe, Coin, Trapdoor	A log cabin in the woods.
Forest	Tree, Key	A deep dark forest.
Cellar	Elf	A dusty cellar below the cabin.
Riverbank	Horn, River	A grassy riverbank beside a fast-flowing stream.
Clearing	Ground	A quiet clearing — the soil looks recently disturbed.
Storeroom	Lumberjack, Log, Shovel, Gold	Storage for unplaced entities.

Each action is defined in actions.xml.
For example:
```xml
<action>
  <triggers>
    <keyphrase>open</keyphrase>
    <keyphrase>unlock</keyphrase>
  </triggers>
  <subjects>
    <entity>trapdoor</entity>
    <entity>key</entity>
  </subjects>
  <consumed>
    <entity>key</entity>
  </consumed>
  <produced>
    <entity>cellar</entity>
  </produced>
  <narration>You unlock the door and see steps leading down into a cellar.</narration>
</action>
```
This design makes the game world fully extensible — new locations, entities and actions can be added without modifying the Java code.



## Objective

Your mission is simple:

    •	Explore the world and uncover the legendary pot of gold.
    •	You’ll need curiosity, observation, and a bit of experimentation to succeed.



## ⚙️ Technical Overview

    •	Language: Java 17
    •	Build System: Maven
    •	Architecture: Client–Server (TCP sockets)
    •	Game Data: Defined via .dot (entities & locations) and .xml (actions & triggers) files
    •	Testing: JUnit functional tests (ExampleSTAGTests.java)


File Structure
```
src/
 ├── main/java/edu/uob/
 │   ├── GameServer.java
 │   ├── GameClient.java
 │   ├── ExecuteBasicCommands.java
 │   ├── ExecuteExtendedCommands.java
 │   └── ...
 └── test/java/edu/uob/
     └── ExampleSTAGTests.java
```



## Running Tests

To verify functionality:
```bash
./mvnw clean test
```
Detailed results can be found in:
```
target/surefire-reports/
```



## Notes

    •	Each game session is interactive and shared between all connected local clients.
    •	The modular design allows you to build your own worlds and define new actions.
    •	The engine is deliberately lightweight — designed to be extended, tested, and explored.



## Ready to Play?

Start the server, join as a client and type:
```
look
```
The adventure awaits.
