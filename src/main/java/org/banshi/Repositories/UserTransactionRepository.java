package org.banshi.Repositories;

import org.banshi.Entities.UserTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTransactionRepository extends JpaRepository<UserTransaction, Long> {

    List<UserTransaction> findByUserUserId(Long userId);
}
