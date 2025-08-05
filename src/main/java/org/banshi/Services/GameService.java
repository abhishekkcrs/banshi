package org.banshi.Services;

import org.banshi.Dtos.DeclareResultRequest;
import org.banshi.Dtos.GameRequest;
import org.banshi.Dtos.GameResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GameService {
    GameResponse createGame(GameRequest game);
    List<GameResponse> getAllGames();
    GameResponse getGameById(Long id);
    void deleteGame(Long  id);
    GameResponse declareGameResult(DeclareResultRequest request);
    String evaluateGameResult(Long gameId);
}
