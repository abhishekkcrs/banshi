package org.banshi.Dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GameResponse {

    private Long gameId;
    private String name;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;
    private String openResult;   // e.g., "123"
    private String closeResult;  // e.g., "789"
}

