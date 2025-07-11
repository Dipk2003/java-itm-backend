package com.itech.itech_backend.service;

import com.itech.itech_backend.model.Lead;
import com.itech.itech_backend.model.User;
import com.itech.itech_backend.enums.LeadStatus;
import com.itech.itech_backend.enums.LeadPriority;
import com.itech.itech_backend.repository.LeadRepository;
import com.itech.itech_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@Service
public class LeadService {

    @Autowired
    private LeadRepository leadRepository;
    
    @Autowired
    private UserRepository userRepository;

    public Lead createLead(Lead lead) {
        lead.setInquiryDate(LocalDateTime.now());
        lead.setLastUpdated(LocalDateTime.now());
        return leadRepository.save(lead);
    }

    public List<Lead> getLeadsByVendor(Long vendorId) {
        User vendor = userRepository.findById(vendorId)
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
        return leadRepository.findByVendor(vendor);
    }

    public List<Lead> getLeadsByVendorAndStatus(Long vendorId, LeadStatus status) {
        User vendor = userRepository.findById(vendorId)
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
        return leadRepository.findByVendorAndStatus(vendor, status);
    }

    public List<Lead> getLeadsByVendorAndPriority(Long vendorId, LeadPriority priority) {
        User vendor = userRepository.findById(vendorId)
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
        return leadRepository.findByVendorAndPriority(vendor, priority);
    }

    public Optional<Lead> getLeadById(Long leadId) {
        return leadRepository.findById(leadId);
    }

    public Lead updateLead(Long leadId, Lead updatedLead) {
        return leadRepository.findById(leadId)
            .map(lead -> {
                lead.setCustomerName(updatedLead.getCustomerName());
                lead.setCustomerEmail(updatedLead.getCustomerEmail());
                lead.setCustomerPhone(updatedLead.getCustomerPhone());
                lead.setCustomerCompany(updatedLead.getCustomerCompany());
                lead.setProductInterest(updatedLead.getProductInterest());
                lead.setStatus(updatedLead.getStatus());
                lead.setPriority(updatedLead.getPriority());
                lead.setEstimatedValue(updatedLead.getEstimatedValue());
                lead.setNotes(updatedLead.getNotes());
                lead.setNextFollowUpDate(updatedLead.getNextFollowUpDate());
                lead.setLastUpdated(LocalDateTime.now());
                return leadRepository.save(lead);
            })
            .orElseThrow(() -> new RuntimeException("Lead not found"));
    }

    public Lead updateLeadStatus(Long leadId, LeadStatus status) {
        return leadRepository.findById(leadId)
            .map(lead -> {
                lead.setStatus(status);
                lead.setLastUpdated(LocalDateTime.now());
                return leadRepository.save(lead);
            })
            .orElseThrow(() -> new RuntimeException("Lead not found"));
    }

    public Lead updateLeadPriority(Long leadId, LeadPriority priority) {
        return leadRepository.findById(leadId)
            .map(lead -> {
                lead.setPriority(priority);
                lead.setLastUpdated(LocalDateTime.now());
                return leadRepository.save(lead);
            })
            .orElseThrow(() -> new RuntimeException("Lead not found"));
    }

    public Lead addNotesToLead(Long leadId, String notes) {
        return leadRepository.findById(leadId)
            .map(lead -> {
                String existingNotes = lead.getNotes() != null ? lead.getNotes() : "";
                String updatedNotes = existingNotes.isEmpty() ? notes : existingNotes + "\n" + notes;
                lead.setNotes(updatedNotes);
                lead.setLastUpdated(LocalDateTime.now());
                return leadRepository.save(lead);
            })
            .orElseThrow(() -> new RuntimeException("Lead not found"));
    }

    public void deleteLead(Long leadId) {
        leadRepository.deleteById(leadId);
    }

    public List<Lead> getOverdueFollowUps(Long vendorId) {
        User vendor = userRepository.findById(vendorId)
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
        return leadRepository.findByVendorAndNextFollowUpDateBefore(vendor, LocalDateTime.now());
    }

    public List<Lead> getRecentLeads(Long vendorId) {
        User vendor = userRepository.findById(vendorId)
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return leadRepository.findRecentLeadsByVendor(vendor, thirtyDaysAgo);
    }

    public Map<String, Long> getLeadStats(Long vendorId) {
        User vendor = userRepository.findById(vendorId)
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
        
        List<Object[]> stats = leadRepository.getLeadStatsByVendor(vendor);
        Map<String, Long> leadStats = new HashMap<>();
        
        // Initialize all statuses with 0
        for (LeadStatus status : LeadStatus.values()) {
            leadStats.put(status.name(), 0L);
        }
        
        // Update with actual counts
        for (Object[] stat : stats) {
            LeadStatus status = (LeadStatus) stat[0];
            Long count = (Long) stat[1];
            leadStats.put(status.name(), count);
        }
        
        // Calculate totals
        long totalLeads = leadStats.values().stream().mapToLong(Long::longValue).sum();
        leadStats.put("TOTAL", totalLeads);
        
        return leadStats;
    }

    public List<Lead> searchLeadsByCustomerName(Long vendorId, String customerName) {
        User vendor = userRepository.findById(vendorId)
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
        return leadRepository.findByVendorAndCustomerNameContainingIgnoreCase(vendor, customerName);
    }

    public List<Lead> findDuplicateLeads(String email, String phone) {
        return leadRepository.findByCustomerEmailOrCustomerPhone(email, phone);
    }

    public List<Lead> getAllLeads() {
        return leadRepository.findAll();
    }
}
