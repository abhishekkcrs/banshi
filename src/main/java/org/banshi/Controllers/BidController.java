package org.banshi.Controllers;

import org.banshi.Dtos.*;
import org.banshi.Services.BidService;
import org.banshi.Services.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bids")
public class BidController {

    private static final Logger logger = LoggerFactory.getLogger(BidController.class);

    @Autowired
    private BidService bidService;

    @Autowired
    private GameService gameService;

    // 1. Place a bid
    @PostMapping("/place")
    public ResponseEntity<ApiResponse<BidResponse>> placeBid(@RequestBody BidRequest request) {
        logger.info("Received request to place bid: userId={}, gameId={}, amount={}",
                request.getUserId(), request.getGameId(), request.getAmount());
        try {
            BidResponse response = bidService.placeBid(request);
            logger.info("Bid placed successfully: bidId={}, userId={}, gameId={}",
                    response.getBidId(), response.getUserId(), response.getGameId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("SUCCESS", "Bid placed successfully", response));
        } catch (Exception e) {
            logger.error("Failed to place bid for userId={} on gameId={}: {}",
                    request.getUserId(), request.getGameId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    // 2. Get all bids by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BidResponse>>> getBidsByUser(@PathVariable Long userId) {
        logger.info("Fetching all bids for userId={}", userId);
        try {
            List<BidResponse> response = bidService.getBidsByUser(userId);
            logger.info("Fetched {} bids for userId={}", response.size(), userId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "User's bids fetched", response));
        } catch (Exception e) {
            logger.warn("No bids found for userId={}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    // 3. Get all bids by game
    @GetMapping("/game/{gameId}")
    public ResponseEntity<ApiResponse<List<BidResponse>>> getBidsByGame(@PathVariable Long gameId) {
        logger.info("Fetching all bids for gameId={}", gameId);
        try {
            List<BidResponse> response = bidService.getBidsByGame(gameId);
            logger.info("Fetched {} bids for gameId={}", response.size(), gameId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Game's bids fetched", response));
        } catch (Exception e) {
            logger.warn("No bids found for gameId={}: {}", gameId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    // 4. Get a bid by ID
    @GetMapping("/{bidId}")
    public ResponseEntity<ApiResponse<BidResponse>> getBidById(@PathVariable Long bidId) {
        logger.info("Fetching bid by bidId={}", bidId);
        try {
            BidResponse response = bidService.getBidById(bidId);
            logger.info("Bid fetched successfully: bidId={}", bidId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Bid found", response));
        } catch (Exception e) {
            logger.warn("Bid not found for bidId={}: {}", bidId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    // 5. Get all bids
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<BidResponse>>> getAllBids() {
        logger.info("Fetching all bids");
        try {
            List<BidResponse> response = bidService.getAllBids();
            logger.info("Fetched {} total bids", response.size());
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "All bids fetched", response));
        } catch (Exception e) {
            logger.error("Error fetching all bids: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }
}
