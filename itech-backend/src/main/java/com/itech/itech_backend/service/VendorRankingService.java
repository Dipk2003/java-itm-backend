package com.itech.itech_backend.service;

import com.itech.itech_backend.model.User;
import com.itech.itech_backend.model.VendorRanking;
import com.itech.itech_backend.repository.VendorRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorRankingService {

    private final VendorRankingRepository rankingRepo;

    public VendorRanking getOrCreateRanking(User vendor) {
        return rankingRepo.findByVendor(vendor)
                .orElseGet(() -> rankingRepo.save(
                        VendorRanking.builder().vendor(vendor).totalLeadsGenerated(0).performanceScore(0).build()));
    }

    public List<VendorRanking> getAllRankings() {
        return rankingRepo.findAll();
    }
}
