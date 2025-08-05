package org.banshi.Services.Impl;

import org.banshi.Dtos.BidRequest;
import org.banshi.Dtos.BidResponse;
import org.banshi.Entities.Bid;
import org.banshi.Entities.Enums.BidResultStatus;
import org.banshi.Entities.Enums.BidType;
import org.banshi.Entities.Enums.TransactionType;
import org.banshi.Entities.Game;
import org.banshi.Entities.User;
import org.banshi.Entities.UserTransaction;
import org.banshi.Exceptions.InsufficientBalanceException;
import org.banshi.Exceptions.ResourceNotFoundException;
import org.banshi.Repositories.BidRepository;
import org.banshi.Repositories.GameRepository;
import org.banshi.Repositories.UserRepository;
import org.banshi.Repositories.UserTransactionRepository;
import org.banshi.Services.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BidServiceImpl implements BidService {

    @Autowired
    private BidRepository bidRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private UserTransactionRepository transactionRepository;

    @Override
    public List<BidResponse> getBidsByUser(Long userId) {
        return bidRepository.findByUserUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BidResponse> getBidsByGame(Long gameId) {
        return bidRepository.findByGameGameId(gameId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BidResponse getBidById(Long bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with ID: " + bidId));
        return mapToResponse(bid);
    }

    @Override
    public List<BidResponse> getAllBids() {

        List<Bid> bids = bidRepository.findAll();
        if (bids.isEmpty()) {
            throw new ResourceNotFoundException("No bids found");
        }

        return bids.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private BidResponse mapToResponse(Bid bid) {
        return BidResponse.builder()
                .bidId(bid.getBidId())
                .userId(bid.getUser().getUserId())
                .gameId(bid.getGame().getGameId())
                .bidType(bid.getBidType().toString())
                .bidTiming(bid.getBidTiming().toString()) // NEW FIELD
                .number(bid.getNumber())
                .amount(bid.getAmount())
                .resultStatus(bid.getResultStatus())
                .placedAt(bid.getPlacedAt())
                .build();
    }

    @Override
    public BidResponse placeBid(BidRequest request) {

        // Ensure valid bidTiming if needed
        if (requiresTiming(request.getBidType()) && request.getBidTiming() == null) {
            throw new IllegalArgumentException("BidTiming is required for " + request.getBidType());
        }

        // Validate number format for type
        validateBidNumber(request.getBidType(), request.getNumber());

        // Fetch user and game
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));

        // ✅ Check user balance
        if (user.getBalance() < request.getAmount()) {
            throw new InsufficientBalanceException("Insufficient balance. Your wallet has ₹" + user.getBalance());
        }

        // ✅ Deduct amount from wallet
        user.setBalance(user.getBalance() - request.getAmount());
        userRepository.save(user); // Save updated balance

        // ✅ Place the bid
        Bid bid = Bid.builder()
                .user(user)
                .game(game)
                .bidType(request.getBidType())
                .bidTiming(request.getBidTiming())
                .number(request.getNumber())
                .amount(request.getAmount())
                .resultStatus(BidResultStatus.PENDING)
                .build();
        Bid savedBid = bidRepository.save(bid);

        // save transaction
        UserTransaction txn = UserTransaction.builder()
                .user(user)
                .amount(request.getAmount())
                .type(TransactionType.DEBIT)
                .description("Bid placed on game: " + game.getName())
                .timestamp(LocalDateTime.now())
                .build();
        transactionRepository.save(txn);

        return mapToResponse(savedBid);
    }

    private boolean requiresTiming(BidType type) {
        // These bid types need OPEN or CLOSE timing
        return type == BidType.SINGLE_DIGIT ||
                type == BidType.SINGLE_PANNA ||
                type == BidType.DOUBLE_PANNA ||
                type == BidType.TRIPLE_PANNA ||
                type == BidType.HALF_SANGAM;
    }

    private void validateBidNumber(BidType bidType, String number) {
        switch (bidType) {
            case SINGLE_DIGIT:
                if (!number.matches("\\d") || Integer.parseInt(number) > 9)
                    throw new IllegalArgumentException("Invalid SINGLE_DIGIT bid");
                break;

            case JODI_DIGIT:
                if (!number.matches("\\d{2}"))
                    throw new IllegalArgumentException("Invalid JODI_DIGIT bid");
                break;

            case SINGLE_PANNA:
                if (!number.matches("\\d{3}") || hasRepeatedDigits(number))
                    throw new IllegalArgumentException("Invalid SINGLE_PANNA bid");
                break;

            case DOUBLE_PANNA:
                if (!number.matches("\\d{3}") || !hasExactlyOnePair(number))
                    throw new IllegalArgumentException("Invalid DOUBLE_PANNA bid");
                break;

            case TRIPLE_PANNA:
                if (!number.matches("(\\d)\\1\\1"))
                    throw new IllegalArgumentException("Invalid TRIPLE_PANNA bid");
                break;

            case HALF_SANGAM:
                if (!number.matches("\\d{1}-\\d{3}") && !number.matches("\\d{3}-\\d{1}"))
                    throw new IllegalArgumentException("Invalid HALF_SANGAM format. Use '4-123' or '123-4'");

                String[] parts = number.split("-");
                if (parts.length != 2)
                    throw new IllegalArgumentException("HALF_SANGAM must be in 'digit-panna' or 'panna-digit' format");

                String part1 = parts[0], part2 = parts[1];
                if ((part1.length() == 1 && !part2.matches("\\d{3}")) ||
                        (part1.length() == 3 && !part2.matches("\\d")))
                    throw new IllegalArgumentException("Invalid HALF_SANGAM digit-panna combination.");
                break;

            case FULL_SANGAM:
                if (!number.matches("\\d{3}-\\d{3}"))
                    throw new IllegalArgumentException("Invalid FULL_SANGAM format. Expected format: '123-456'");
                break;

            default:
                throw new IllegalArgumentException("Unsupported BidType");
        }
    }

    private boolean hasRepeatedDigits(String number) {
        return number.chars().distinct().count() < number.length();
    }

    private boolean hasExactlyOnePair(String number) {
        Map<Character, Long> freq = number.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return freq.containsValue(2L) && freq.size() == 2;
    }

}
