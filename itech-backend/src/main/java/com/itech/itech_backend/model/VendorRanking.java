package com.itech.itech_backend.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class VendorRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getVendor() {
        return vendor;
    }

    public void setVendor(User vendor) {
        this.vendor = vendor;
    }

    public int getTotalLeads() {
        return totalLeads;
    }

    public void setTotalLeads(int totalLeads) {
        this.totalLeads = totalLeads;
    }

    public int getLeadsConverted() {
        return leadsConverted;
    }

    public void setLeadsConverted(int leadsConverted) {
        this.leadsConverted = leadsConverted;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public double getAvgResponseTime() {
        return avgResponseTime;
    }

    public void setAvgResponseTime(double avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }

    public double getRankScore() {
        return rankScore;
    }

    public void setRankScore(double rankScore) {
        this.rankScore = rankScore;
    }

    @OneToOne
    private User vendor;

    private int totalLeads;
    private int leadsConverted;
    private double rating;
    private double avgResponseTime;
    private double rankScore;
}
