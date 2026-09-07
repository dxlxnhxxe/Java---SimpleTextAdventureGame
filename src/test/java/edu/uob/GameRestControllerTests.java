package edu.uob;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uob.dto.CommandRequest;
import edu.uob.dto.CreateGameRequest;
import edu.uob.dto.JoinGameRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class GameRestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGameLifecycleViaRest() throws Exception {
        // 1. Create a game
        CreateGameRequest createRequest = new CreateGameRequest("Manor Test Game", "extended");
        MvcResult createResult = mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").exists())
                .andExpect(jsonPath("$.gameName").value("Manor Test Game"))
                .andExpect(jsonPath("$.startingLocation").value("cabin"))
                .andReturn();

        JsonNode createNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String gameId = createNode.get("gameId").asText();

        // 2. List games
        mockMvc.perform(get("/api/v1/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 3. Get specific game
        mockMvc.perform(get("/api/v1/games/" + gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId));

        // 4. Join game
        JoinGameRequest joinRequest = new JoinGameRequest("Alice");
        mockMvc.perform(post("/api/v1/games/" + gameId + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.playerName").value("Alice"))
                .andExpect(jsonPath("$.currentLocation").value("cabin"))
                .andExpect(jsonPath("$.health").value(3))
                .andExpect(jsonPath("$.availablePaths").isArray())
                .andExpect(jsonPath("$.availableArtefacts").isArray())
                .andExpect(jsonPath("$.availableExtendedCommands").isArray());

        // 5. Execute "look" command
        CommandRequest lookCommand = new CommandRequest("Alice", "look");
        mockMvc.perform(post("/api/v1/games/" + gameId + "/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lookCommand)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLocation").value("cabin"))
                .andExpect(jsonPath("$.availablePaths").isArray())
                .andExpect(jsonPath("$.availableExtendedCommands").isArray())
                .andExpect(jsonPath("$.narrative").value(org.hamcrest.Matchers.containsString("cabin")));

        // 6. Execute "get potion" command
        CommandRequest getPotion = new CommandRequest("Alice", "get potion");
        mockMvc.perform(post("/api/v1/games/" + gameId + "/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getPotion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventory").isArray())
                .andExpect(jsonPath("$.inventory[0]").value("potion"));

        // 7. Execute "goto forest" command
        CommandRequest gotoForest = new CommandRequest("Alice", "goto forest");
        mockMvc.perform(post("/api/v1/games/" + gameId + "/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gotoForest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLocation").value("forest"));
    }

    @Test
    void testInvalidGameReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/games/non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testBlankPlayerNameReturns400() throws Exception {
        CreateGameRequest createRequest = new CreateGameRequest("Validation Test", "extended");
        MvcResult createResult = mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String gameId = createNode.get("gameId").asText();

        JoinGameRequest badJoin = new JoinGameRequest("   ");
        mockMvc.perform(post("/api/v1/games/" + gameId + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badJoin)))
                .andExpect(status().isBadRequest());
    }
}
