package org.banshi.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.banshi.Entities.Enums.TransactionType;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type; // CREDIT / DEBIT

    private String description;

    private LocalDateTime timestamp;
}
