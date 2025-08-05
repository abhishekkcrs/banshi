package org.banshi.Repositories;

import org.banshi.Entities.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByUserUserId(Long userId);
    List<Bid> findByGameGameId(Long gameId);

}
