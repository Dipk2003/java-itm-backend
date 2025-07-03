package com.itech.itech_backend.service;

import com.itech.itech_backend.model.User;
import com.itech.itech_backend.model.VendorRanking;
import com.itech.itech_backend.repository.VendorRankingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VendorRankingService {

    @Autowired
    private VendorRankingRepository vendorRankingRepository;

    public double calculateScore(int leadsConverted, double rating, double responseTime) {
        return (0.5 * leadsConverted) + (0.3 * rating) - (0.2 * responseTime);
    }

    public void updateRanking(User vendor, int leads, int converted, double rating, double responseTime) {
        Optional<VendorRanking> optional = vendorRankingRepository.findByVendor(vendor);

        VendorRanking ranking = optional.orElse(new VendorRanking());
        ranking.setVendor(vendor);
        ranking.setTotalLeads(leads);
        ranking.setLeadsConverted(converted);
        ranking.setRating(rating);
        ranking.setAvgResponseTime(responseTime);
        ranking.setRankScore(calculateScore(converted, rating, responseTime));

        vendorRankingRepository.save(ranking);
    }
}
