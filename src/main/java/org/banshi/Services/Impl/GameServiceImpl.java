package org.banshi.Services.Impl;

import org.banshi.Dtos.DeclareResultRequest;
import org.banshi.Dtos.GameRequest;
import org.banshi.Dtos.GameResponse;
import org.banshi.Entities.Bid;
import org.banshi.Entities.Enums.BidResultStatus;
import org.banshi.Entities.Enums.BidTiming;
import org.banshi.Entities.Enums.BidType;
import org.banshi.Entities.Enums.TransactionType;
import org.banshi.Entities.Game;
import org.banshi.Entities.User;
import org.banshi.Entities.UserTransaction;
import org.banshi.Exceptions.ResourceNotFoundException;
import org.banshi.Repositories.BidRepository;
import org.banshi.Repositories.GameRepository;
import org.banshi.Repositories.UserRepository;
import org.banshi.Repositories.UserTransactionRepository;
import org.banshi.Services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameServiceImpl implements GameService {

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private BidRepository bidRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserTransactionRepository userTransactionRepository;

    @Override
    public GameResponse createGame(GameRequest request) {
        Game game = Game.builder()
                .name(request.getName())
                .openingTime(request.getOpeningTime())
                .closingTime(request.getClosingTime())
                .gameResult(null)
                .openResult(null)
                .closeResult(null)
                .build();

        return mapToResponse(gameRepository.save(game));
    }

    @Override
    public List<GameResponse> getAllGames() {
        List<Game> games = gameRepository.findAll();

        if (games.isEmpty()) {
            throw new ResourceNotFoundException("No games found");
        }

        return games.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public GameResponse getGameById(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with ID: " + id));
        return mapToResponse(game);
    }

    @Override
    public void deleteGame(Long id) {
        if (!gameRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete — game not found with ID: " + id);
        }
        gameRepository.deleteById(id);
    }

    @Override
    public GameResponse declareGameResult(DeclareResultRequest request) {
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with ID: " + request.getGameId()));

        if(game.getGameResult()!= null) {
            throw new IllegalStateException("Game result already declared");
        }

        String openResult = request.getOpenResult();
        String closeResult = request.getCloseResult();

        String jodi = "" + openResult.chars().map(Character::getNumericValue).sum() % 10 + closeResult.chars().map(Character::getNumericValue).sum() % 10;
        game.setOpenResult(openResult);   // "45"
        game.setCloseResult(closeResult);  // "678"

        game.setGameResult(openResult + "-" + jodi + "-" + closeResult);
        Game updatedGame = gameRepository.save(game);
        return mapToResponse(updatedGame);
    }

    private GameResponse mapToResponse(Game game) {
        return GameResponse.builder()
                .gameId(game.getGameId())
                .name(game.getName())
                .openingTime(game.getOpeningTime())
                .closingTime(game.getClosingTime())
                .openResult(game.getOpenResult())
                .closeResult(game.getCloseResult())
                .build();
    }

    @Override
    public String evaluateGameResult(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));

        String openResult = game.getOpenResult();
        String closeResult = game.getCloseResult();
        String result = game.getGameResult();

        if (openResult == null || closeResult == null) {
            throw new IllegalArgumentException("Game results are not declared yet.");
        }

        String openDigit = String.valueOf(result.charAt(4));
        String closeDigit = String.valueOf(result.charAt(5));
        String jodi = openDigit + closeDigit;

        List<Bid> bids = bidRepository.findByGameGameId(gameId);

        for (Bid bid : bids) {
            try {
                // Skip already evaluated
                if (bid.getResultStatus() != BidResultStatus.PENDING) continue;

                String bidNum = bid.getNumber();
                boolean won = false;

                switch (bid.getBidType()) {
                    case SINGLE_DIGIT:
                        if (bid.getBidTiming() == BidTiming.OPEN && bidNum.equals(openDigit)) won = true;
                        else if (bid.getBidTiming() == BidTiming.CLOSE && bidNum.equals(closeDigit)) won = true;
                        break;

                    case JODI_DIGIT:
                        if (bidNum.equals(jodi)) won = true;
                        break;

                    case SINGLE_PANNA:
                    case DOUBLE_PANNA:
                    case TRIPLE_PANNA:
                        if (bid.getBidTiming() == BidTiming.OPEN && bidNum.equals(openResult)) won = true;
                        else if (bid.getBidTiming() == BidTiming.CLOSE && bidNum.equals(closeResult)) won = true;
                        break;

                    case HALF_SANGAM:
                        String[] half = bidNum.split("-");
                        if (half.length == 2) {
                            if ((half[0].equals(openDigit) && half[1].equals(closeResult)) ||
                                    (half[0].equals(closeDigit) && half[1].equals(openResult))) {
                                won = true;
                            }
                        }
                        break;

                    case FULL_SANGAM:
                        String[] full = bidNum.split("-");
                        if (full.length == 2 && full[0].equals(openResult) && full[1].equals(closeResult)) {
                            won = true;
                        }
                        break;
                }

                if (won) {
                    bid.setResultStatus(BidResultStatus.WON);
                    double reward = calculateReward(bid.getBidType(), bid.getAmount());

                    User user = bid.getUser();
                    user.setBalance(user.getBalance() + reward); // ✅ Update balance
                    userRepository.save(user);

                    // ✅ Log credit transaction
                    UserTransaction txn = UserTransaction.builder()
                            .user(user)
                            .amount(reward)
                            .type(TransactionType.CREDIT)
                            .description("Bid WON (Type: " + bid.getBidType() + ")")
                            .build();

                    userTransactionRepository.save(txn);
                } else {
                    bid.setResultStatus(BidResultStatus.LOST);
                }

                bidRepository.save(bid);

            } catch (Exception ex) {
                System.err.println("Failed to evaluate bid ID: " + bid.getBidId() + ". Reason: " + ex.getMessage());
            }
        }

        return "Result evaluated successfully for game ID: " + gameId;
    }

    private double calculateReward(BidType bidType, double amount) {
        return switch (bidType) {
            case SINGLE_DIGIT -> amount * 9.0;
            case JODI_DIGIT -> amount * 90.0;
            case SINGLE_PANNA -> amount * 140.0;
            case DOUBLE_PANNA -> amount * 280.0;
            case TRIPLE_PANNA -> amount * 600.0;
            case HALF_SANGAM -> amount * 1000.0;
            case FULL_SANGAM -> amount * 10000.0;
            default -> amount; // fallback
        };
    }

}
