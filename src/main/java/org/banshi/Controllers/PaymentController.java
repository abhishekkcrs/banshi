package org.banshi.Controllers;

import org.banshi.Dtos.ApiResponse;
import org.banshi.Dtos.OrderResponse;
import org.banshi.Dtos.VerifyPaymentRequest;
import org.banshi.Services.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order/{userId}")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@PathVariable Long userId, @RequestParam Double amount) {
        try {
            OrderResponse order = paymentService.createOrder(userId, amount);
            return ResponseEntity.ok(new ApiResponse<>("success", "Order created successfully", order));
        } catch (Exception e) {
            logger.error("Error creating order for userId={}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyPayment(@RequestBody VerifyPaymentRequest request) {
        try {
            boolean verified = paymentService.verifyPayment(request);
            return ResponseEntity.ok(new ApiResponse<>("success", verified ? "Payment verified" : "Payment failed", verified));
        } catch (Exception e) {
            logger.error("Error verifying payment", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("error", e.getMessage(), false));
        }
    }
}
