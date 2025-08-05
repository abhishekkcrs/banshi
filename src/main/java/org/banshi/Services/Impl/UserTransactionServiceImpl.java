package org.banshi.Services.Impl;

import org.banshi.Dtos.UserTransactionResponse;
import org.banshi.Entities.UserTransaction;
import org.banshi.Repositories.UserTransactionRepository;
import org.banshi.Services.UserTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserTransactionServiceImpl implements UserTransactionService {

    @Autowired
    private UserTransactionRepository transactionRepository;

    @Override
    public List<UserTransactionResponse> getTransactionsByUser(Long userId) {
        List<UserTransaction> transactions = transactionRepository.findByUserUserId(userId);

        if (transactions == null || transactions.isEmpty()) {
            throw new IllegalArgumentException("No transactions found for user with ID: " + userId);
        }

        return transactions.stream()
                .map(txn -> UserTransactionResponse.builder()
                        .transactionId(txn.getTransactionId())
                        .amount(txn.getAmount())
                        .type(txn.getType())
                        .description(txn.getDescription())
                        .timestamp(txn.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }

}

