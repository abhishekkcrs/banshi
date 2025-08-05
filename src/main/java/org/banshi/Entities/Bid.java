package org.banshi.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.banshi.Entities.Enums.BidResultStatus;
import org.banshi.Entities.Enums.BidTiming;
import org.banshi.Entities.Enums.BidType;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bidId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    private BidType bidType; // E.g., SINGLE_DIGIT, JODI, etc.

    @Enumerated(EnumType.STRING)
    private BidTiming bidTiming; // NEW FIELD: OPEN or CLOSE

    private String number;  // Could be 1-digit, 2-digit, 3-digit

    private Double amount;

    @Enumerated(EnumType.STRING)
    private BidResultStatus resultStatus;

    @CreationTimestamp
    private LocalDateTime placedAt;
}
