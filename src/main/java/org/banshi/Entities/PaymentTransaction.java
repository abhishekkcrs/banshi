package org.banshi.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.banshi.Entities.Enums.PaymentStatus;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @ManyToOne
    private User user;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
