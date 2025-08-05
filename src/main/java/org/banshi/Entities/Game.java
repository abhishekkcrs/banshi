package org.banshi.Entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gameId;

    private String name;

    private LocalDateTime openingTime;

    private LocalDateTime closingTime;

    private String openResult;   // e.g., "123"
    private String closeResult;  // e.g., "789"

    private String gameResult;

}
