package edu.uob.controller;

import edu.uob.dto.CommandRequest;
import edu.uob.dto.CommandResponse;
import edu.uob.service.GameEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/games")
@CrossOrigin(origins = "*")
public class CommandController {

    private final GameEngineService gameEngineService;

    public CommandController(GameEngineService gameEngineService) {
        this.gameEngineService = gameEngineService;
    }

    @PostMapping("/{id}/command")
    public ResponseEntity<CommandResponse> executeCommand(
            @PathVariable("id") String gameId,
            @RequestBody CommandRequest request) {
        CommandResponse response = gameEngineService.executeCommand(
                gameId,
                request.getPlayerName(),
                request.getCommand()
        );
        return ResponseEntity.ok(response);
    }
}
