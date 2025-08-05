package org.banshi.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.banshi.Entities.Enums.BidTiming;
import org.banshi.Entities.Enums.BidType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BidRequest {
    private Long userId;
    private Long gameId;
    private BidType bidType;
    private BidTiming bidTiming; // NEW FIELD
    private String number;
    private Double amount;
}
