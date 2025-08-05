package org.banshi.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.banshi.Entities.Enums.TransactionType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserTransactionResponse {

    private Long transactionId;
    private Double amount;
    private TransactionType type;
    private String description;
    private LocalDateTime timestamp;
}

