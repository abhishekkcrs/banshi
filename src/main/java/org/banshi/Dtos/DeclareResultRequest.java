package org.banshi.Dtos;

import lombok.Data;

@Data
public class DeclareResultRequest {
    private Long gameId;
    private String openResult;   // e.g., "123"
    private String closeResult;  // e.g., "789"
}
