# FirstJoinWarning Plugin

A Minecraft Plugin created for The Corner MC. After rumours of previous servers being used to steal player IP addresses, it was important to remind players of various risks associated to trusting random internet people who help a bald 45 year old man as a hobby.

## Warnings Displayed

### Warning #1 - VPN Usage
Informs players about the importance of using a VPN when connecting to community-run servers and the risks of sharing their IP address.

### Warning #2 - Username History
Warns players that their Minecraft username history is publicly accessible and cannot be hidden. Recommends visiting NameMC where they can view their complete username history before continuing.

### Warning #3 - Final Confirmation
A brief final message confirming they've read all warnings and informing them that the next time they join, they will be connected to the server.

## Building

Requires:
- Java 21
- Maven

To build:
```bash
mvn clean package
```

The compiled JAR will be in the `target/` directory.

## Installation

1. Build the plugin or download the compiled JAR
2. Place `FirstJoinWarning-1.0.0.jar` in your Paper server's `plugins/` directory
3. Restart or reload your server

## Usage

The plugin works automatically. When a new player joins:
1. They'll see Warning #1 and be kicked
2. When they rejoin, they'll see Warning #2 and be kicked
3. When they rejoin again, they'll see Warning #3 and be kicked
4. On their fourth join attempt, they'll be allowed to join normally

## Data Storage

Player warning progress is stored in `plugins/FirstJoinWarning/playerdata.yml`
