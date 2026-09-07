package edu.uob.controller;

import edu.uob.dto.*;
import edu.uob.service.GameEngineService;
import edu.uob.service.GamePersistenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/games")
@CrossOrigin(origins = "*")
public class GameSessionController {

    private final GameEngineService gameEngineService;
    private final GamePersistenceService gamePersistenceService;

    public GameSessionController(GameEngineService gameEngineService, GamePersistenceService gamePersistenceService) {
        this.gameEngineService = gameEngineService;
        this.gamePersistenceService = gamePersistenceService;
    }

    @PostMapping
    public ResponseEntity<GameSessionResponse> createGame(@RequestBody(required = false) CreateGameRequest request) {
        String gameName = (request != null) ? request.getGameName() : "STAG Adventure";
        String template = (request != null) ? request.getTemplate() : "extended";
        GameSessionResponse response = gameEngineService.createGame(gameName, template);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<GameSessionResponse>> listGames() {
        return ResponseEntity.ok(gameEngineService.listGames());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameSessionResponse> getGame(@PathVariable("id") String gameId) {
        return ResponseEntity.ok(gameEngineService.getGame(gameId));
    }

    @PostMapping("/{id}/players")
    public ResponseEntity<JoinGameResponse> joinGamePlayers(
            @PathVariable("id") String gameId,
            @RequestBody JoinGameRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                gameEngineService.joinGame(gameId, request.getPlayerName()));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<JoinGameResponse> joinGame(
            @PathVariable("id") String gameId,
            @RequestBody JoinGameRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                gameEngineService.joinGame(gameId, request.getPlayerName()));
    }

    @PostMapping("/{id}/save")
    public ResponseEntity<SaveGameResponse> saveGame(
            @PathVariable("id") String gameId,
            @RequestBody SaveGameRequest request) {
        return ResponseEntity.ok(gamePersistenceService.saveGame(gameId, request.getSaveSlotName()));
    }

    @PostMapping("/load/{saveSlotName}")
    public ResponseEntity<GameSessionResponse> loadGameBySlot(
            @PathVariable("saveSlotName") String saveSlotName) {
        return ResponseEntity.ok(gamePersistenceService.loadGame(saveSlotName));
    }

    @PostMapping("/{id}/load")
    public ResponseEntity<GameSessionResponse> loadGame(
            @PathVariable("id") String gameId,
            @RequestBody(required = false) SaveGameRequest request) {
        String slot = (request != null && request.getSaveSlotName() != null)
                ? request.getSaveSlotName()
                : gameId;
        return ResponseEntity.ok(gamePersistenceService.loadGame(slot));
    }

    @GetMapping("/saves")
    public ResponseEntity<List<SaveGameResponse>> listSavedGames() {
        return ResponseEntity.ok(gamePersistenceService.listSavedGames());
    }
}
