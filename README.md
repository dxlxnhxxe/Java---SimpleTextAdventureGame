# Web-Based Text Adventure Game (TAG)

A modern, full-stack multiplayer text-adventure platform built with **Java 17**, **Spring Boot 3**, **Spring Data JPA**, **PostgreSQL**, **WebSockets & STOMP**, **Spring Security (JWT)** and a **Retro CRT Web Terminal UI**.

---

## Key Architecture & Features

```text
                                  ┌───────────────────────────┐
                                  │         Web Browser       │
                                  └─────────────┬─────────────┘
                                                │ HTTP REST / STOMP WebSocket
                                                ▼
                                 ┌─────────────────────────────┐
                                 │    Spring Boot 3 Backend    │
                                 ├─────────────────────────────┤
                                 │ REST Controllers (/api/v1)  │
                                 │ WebSocket Broker (/ws-game) │
                                 │ Spring Security & JWT       │
                                 └──────────────┬──────────────┘
                                                │
                                                ▼
                                 ┌─────────────────────────────┐
                                 │    Decoupled TAG Engine     │
                                 │                             │
                                 │ GameWorld (Multi-Session)   │
                                 │ Basic & Extended Executors  │
                                 │ Dynamic DOT & XML Parser    │
                                 └──────────────┬──────────────┘
                                                │
                                                ▼
                                 ┌─────────────────────────────┐
                                 │    PostgreSQL Persistence   │
                                 │                             │
                                 │ Game Sessions & Checkpoints │
                                 │ Player States & Inventories │
                                 │ Location & World Placements │
                                 └─────────────────────────────┘
```

- **Instance-Based Multi-Session Game Engine**: Decoupled from legacy static global state into isolated, concurrent `GameWorld` instances.
- **RESTful Game Management APIs**: Create sessions, list games, inspect state and execute commands via clean JSON contracts.
- **Real-Time Multiplayer Synchronization**: WebSocket and STOMP message broker broadcasting room-level and global events (player entry/exit, item drops, interactions) live to all connected players.
- **Relational Persistence**: PostgreSQL with Spring Data JPA for snapshotting and resuming world states, inventories, health and entity placements.
- **Stateless Authentication**: Spring Security 6 with BCrypt password hashing and JWT Bearer tokens.
- **Retro CRT Web Terminal**: Built-in responsive CRT-styled web terminal featuring scanlines, player HUD, keyboard history navigation and quick-action toolbars.
- **Containerized & CI-Ready**: Multi-stage `Dockerfile`, `docker-compose.yml` for single-command deployment and GitHub Actions CI workflow.

---

## Quick Start

### Option A: Single Command with Docker Compose
Run the entire platform (Spring Boot backend + PostgreSQL database) with zero local configuration:

```bash
docker compose up --build
```
Open your browser at **http://localhost:8080** to start playing.

---

### Option B: Local Maven Execution
Run locally with embedded in-memory database:

```bash
./mvnw clean spring-boot:run
```
Visit **http://localhost:8080** to access the web terminal interface.

---

## 🎮 Gameplay & Commands

### Canonical Basic Commands
| Command | Alias | Description |
|---|---|---|
| `look` | — | Inspect your current location, other players, artefacts, furniture and exits. |
| `inventory` | `inv` | View all items currently carried in your inventory. |
| `get <item>` | — | Pick up an artefact from the current location. |
| `drop <item>` | — | Drop an artefact from your inventory into your current location. |
| `goto <location>` | — | Move to an accessible connected location. |
| `health` | — | Check your current health level. |

### Dynamic Extended Actions (XML-Driven)
The world supports dynamic interactions defined in `config/extended-actions.xml`, including:
- `unlock trapdoor with key` / `open trapdoor`
- `chop tree with axe`
- `drink potion`
- `bridge river with log`
- `pay elf with coin`
- `dig ground with shovel`

---

## API Reference

### Authentication (`/api/v1/auth`)
- `POST /api/v1/auth/register` — Register a new player account: `{ "username": "alice", "password": "password123" }`
- `POST /api/v1/auth/login` — Login and receive JWT bearer token: `{ "username": "alice", "password": "password123" }`
- `GET /api/v1/auth/me` — Retrieve current authenticated user profile (`Authorization: Bearer <token>`)

### 🕹️ Game Sessions (`/api/v1/games`)
- `POST /api/v1/games` — Create a new game session: `{ "gameName": "The Old Manor", "template": "extended" }`
- `GET /api/v1/games` — List all active in-memory game sessions
- `GET /api/v1/games/{id}` — Get status and connected players for a game session
- `POST /api/v1/games/{id}/join` — Join a game session: `{ "playerName": "Alice" }`
- `POST /api/v1/games/{id}/command` — Execute a command: `{ "playerName": "Alice", "command": "look" }`
- `POST /api/v1/games/{id}/save` — Save game state to checkpoint: `{ "saveSlotName": "checkpoint-1" }`
- `POST /api/v1/games/load/{saveSlotName}` — Resume a saved game checkpoint from PostgreSQL
- `GET /api/v1/games/saves` — List all persisted game saves

### Real-Time WebSockets (`/ws-game`)
- **STOMP Endpoint**: `/ws-game`
- **Room Location Topic**: `/topic/games/{gameId}/locations/{locationName}` (Broadcasts player arrival/departure and actions in the room)
- **Global Event Topic**: `/topic/games/{gameId}/global` (Broadcasts public announcements and global chats)
- **User Notification Queue**: `/user/queue/notifications`

---

## Testing & Verification

Run the full automated test suite containing core engine regression tests, multi-world concurrency tests, REST MockMvc tests, JPA persistence tests and Web UI tests:

```bash
./mvnw clean test
```

### Test Coverage Highlights
- `ExampleTAGTests`: 22 canonical TAG game mechanics and progression tests.
- `MultiWorldTests`: Concurrency and state-isolation tests between independent `GameWorld` instances.
- `GameRestControllerTests`: MockMvc validation of REST endpoints, command execution and error handling.
- `GamePersistenceTests`: Relational database save/load integration tests verifying world state recovery.
- `AuthSecurityTests`: User registration, BCrypt hashing, JWT issuance and protected endpoint verification.
- `MultiplayerWebSocketTests`: Room-based location pub/sub event verification.
- `WebUiResourceTests`: Static web terminal asset delivery tests.

---

## Project Structure

```text
cw-stag/
├── .github/workflows/ci.yml # Automated CI pipeline
├── config/                  # DOT entity graphs & XML action definitions
├── Dockerfile               # Multi-stage container build
├── docker-compose.yml       # Production stack (Spring Boot + PostgreSQL)
├── pom.xml                  # Maven configuration (Spring Boot 3 + Java 17)
└── src/
    ├── main/
    │   ├── java/edu/uob/
    │   │   ├── config/      # Spring Security, WebSocket and Web MVC config
    │   │   ├── controller/  # REST endpoints (GameSessionController, AuthController, CommandController)
    │   │   ├── dto/         # Request & Response Data Transfer Objects
    │   │   ├── persistence/ # JPA entities (GameSession, Player, Location) & Repositories
    │   │   ├── security/    # JWT token provider & authentication filter
    │   │   ├── service/     # GameEngineService, GamePersistenceService, UserService
    │   │   ├── websocket/   # STOMP controller & GameEventPublisher
    │   │   └── ...          # Decoupled TAG Engine (GameWorld, Parsers, Tokenisers, Executors)
    │   └── resources/
    │       ├── application.yml # Spring Boot configuration
    │       └── static/         # Retro CRT Web Terminal UI (HTML5, CSS3, JS)
    └── test/java/edu/uob/   # Comprehensive unit & integration test suites
```
