package org.banshi.Dtos;

import lombok.Data;

@Data
public class OrderRequest {
    private Long userId;
    private Double amount;
}
