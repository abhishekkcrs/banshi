package org.banshi.Controllers;

import org.banshi.Dtos.ApiResponse;
import org.banshi.Dtos.DeclareResultRequest;
import org.banshi.Dtos.GameRequest;
import org.banshi.Dtos.GameResponse;
import org.banshi.Services.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    @Autowired
    private GameService gameService;

    // 1. Create Game (Admin)
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<GameResponse>> createGame(@RequestBody GameRequest game) {
        logger.info("Received request to create a game: {}", game);
        try {
            GameResponse savedGame = gameService.createGame(game);
            logger.info("Game created successfully with ID: {}", savedGame.getGameId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("SUCCESS", "Game created successfully", savedGame));
        } catch (Exception e) {
            logger.error("Error creating game: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    // 2. Get all games
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<GameResponse>>> getAllGames() {
        logger.info("Fetching all games");
        try {
            List<GameResponse> games = gameService.getAllGames();
            logger.info("Fetched {} games", games.size());
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "All games fetched successfully", games));
        } catch (Exception e) {
            logger.error("Error fetching all games: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    // 3. Get game by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GameResponse>> getGameById(@PathVariable Long id) {
        logger.info("Fetching game with ID: {}", id);
        try {
            GameResponse game = gameService.getGameById(id);
            logger.info("Game fetched successfully: {}", id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Game fetched successfully", game));
        } catch (Exception e) {
            logger.warn("Game not found with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    // 4. Delete game by ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteGame(@PathVariable Long id) {
        logger.info("Request to delete game with ID: {}", id);
        try {
            gameService.deleteGame(id);
            logger.info("Game deleted successfully with ID: {}", id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Game deleted successfully", "Game ID: " + id));
        } catch (Exception e) {
            logger.error("Error deleting game with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    // 5. Declare result
    @PutMapping("/declare-result")
    public ResponseEntity<ApiResponse<GameResponse>> declareGameResult(@RequestBody DeclareResultRequest request) {
        logger.info("Declaring result for gameId: {}", request.getGameId());
        try {
            GameResponse updatedGame = gameService.declareGameResult(request);
            logger.info("Result declared for gameId: {}. Evaluating result...", request.getGameId());

            // Automatically evaluate the result
            gameService.evaluateGameResult(request.getGameId());
            logger.info("Result evaluated for gameId: {}", request.getGameId());

            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Result declared and evaluated", updatedGame));
        } catch (Exception e) {
            logger.error("Error declaring result for gameId {}: {}", request.getGameId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    // 6. Manually evaluate game result
    @PutMapping("/evaluate/{gameId}")
    public ResponseEntity<ApiResponse<String>> evaluateGameResult(@PathVariable Long gameId) {
        logger.info("Manually evaluating result for gameId: {}", gameId);
        try {
            String msg = gameService.evaluateGameResult(gameId);
            logger.info("Game result evaluated for gameId {}: {}", gameId, msg);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", msg, msg));
        } catch (Exception e) {
            logger.error("Error evaluating game result for gameId {}: {}", gameId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }
}
