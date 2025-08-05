package org.banshi.Services;

import java.util.List;
import org.banshi.Dtos.UserTransactionResponse;

public interface UserTransactionService {
    List<UserTransactionResponse> getTransactionsByUser(Long userId);
}
