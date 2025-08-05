package org.banshi.Controllers;

import org.banshi.Dtos.ApiResponse;
import org.banshi.Dtos.UserTransactionResponse;
import org.banshi.Services.UserTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class UserTransactionController {

    @Autowired
    private UserTransactionService transactionService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<UserTransactionResponse>>> getUserTransactions(@PathVariable Long userId) {
        try {
            List<UserTransactionResponse> transactions = transactionService.getTransactionsByUser(userId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Transactions fetched successfully", transactions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }
}
