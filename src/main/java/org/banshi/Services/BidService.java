package org.banshi.Services;

import org.banshi.Dtos.BidRequest;
import org.banshi.Dtos.BidResponse;

import java.util.List;

public interface BidService {
    BidResponse placeBid(BidRequest request);
    List<BidResponse> getBidsByUser(Long userId);
    List<BidResponse> getBidsByGame(Long gameId);
    BidResponse getBidById(Long bidId);

    List<BidResponse> getAllBids();
}
