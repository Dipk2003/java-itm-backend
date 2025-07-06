package com.itech.itech_backend.controller;

import com.itech.itech_backend.model.Lead;
import com.itech.itech_backend.enums.LeadStatus;
import com.itech.itech_backend.enums.LeadPriority;
import com.itech.itech_backend.service.LeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/leads")
@CrossOrigin(origins = "*")
public class LeadController {

    @Autowired
    private LeadService leadService;

    @PostMapping
    public ResponseEntity<Lead> createLead(@RequestBody Lead lead) {
        try {
            Lead createdLead = leadService.createLead(lead);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdLead);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<Lead>> getLeadsByVendor(@PathVariable Long vendorId) {
        try {
            List<Lead> leads = leadService.getLeadsByVendor(vendorId);
            return ResponseEntity.ok(leads);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/vendor/{vendorId}/status/{status}")
    public ResponseEntity<List<Lead>> getLeadsByVendorAndStatus(
            @PathVariable Long vendorId, 
            @PathVariable LeadStatus status) {
        try {
            List<Lead> leads = leadService.getLeadsByVendorAndStatus(vendorId, status);
            return ResponseEntity.ok(leads);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/vendor/{vendorId}/priority/{priority}")
    public ResponseEntity<List<Lead>> getLeadsByVendorAndPriority(
            @PathVariable Long vendorId, 
            @PathVariable LeadPriority priority) {
        try {
            List<Lead> leads = leadService.getLeadsByVendorAndPriority(vendorId, priority);
            return ResponseEntity.ok(leads);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{leadId}")
    public ResponseEntity<Lead> getLeadById(@PathVariable Long leadId) {
        Optional<Lead> lead = leadService.getLeadById(leadId);
        if (lead.isPresent()) {
            return ResponseEntity.ok(lead.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{leadId}")
    public ResponseEntity<Lead> updateLead(@PathVariable Long leadId, @RequestBody Lead updatedLead) {
        try {
            Lead lead = leadService.updateLead(leadId, updatedLead);
            return ResponseEntity.ok(lead);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PatchMapping("/{leadId}/status")
    public ResponseEntity<Lead> updateLeadStatus(
            @PathVariable Long leadId, 
            @RequestParam LeadStatus status) {
        try {
            Lead lead = leadService.updateLeadStatus(leadId, status);
            return ResponseEntity.ok(lead);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PatchMapping("/{leadId}/priority")
    public ResponseEntity<Lead> updateLeadPriority(
            @PathVariable Long leadId, 
            @RequestParam LeadPriority priority) {
        try {
            Lead lead = leadService.updateLeadPriority(leadId, priority);
            return ResponseEntity.ok(lead);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PatchMapping("/{leadId}/notes")
    public ResponseEntity<Lead> addNotesToLead(
            @PathVariable Long leadId, 
            @RequestParam String notes) {
        try {
            Lead lead = leadService.addNotesToLead(leadId, notes);
            return ResponseEntity.ok(lead);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{leadId}")
    public ResponseEntity<Void> deleteLead(@PathVariable Long leadId) {
        try {
            leadService.deleteLead(leadId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/vendor/{vendorId}/overdue")
    public ResponseEntity<List<Lead>> getOverdueFollowUps(@PathVariable Long vendorId) {
        try {
            List<Lead> overdueLeads = leadService.getOverdueFollowUps(vendorId);
            return ResponseEntity.ok(overdueLeads);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/vendor/{vendorId}/recent")
    public ResponseEntity<List<Lead>> getRecentLeads(@PathVariable Long vendorId) {
        try {
            List<Lead> recentLeads = leadService.getRecentLeads(vendorId);
            return ResponseEntity.ok(recentLeads);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/vendor/{vendorId}/stats")
    public ResponseEntity<Map<String, Long>> getLeadStats(@PathVariable Long vendorId) {
        try {
            Map<String, Long> stats = leadService.getLeadStats(vendorId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/vendor/{vendorId}/search")
    public ResponseEntity<List<Lead>> searchLeadsByCustomerName(
            @PathVariable Long vendorId, 
            @RequestParam String customerName) {
        try {
            List<Lead> leads = leadService.searchLeadsByCustomerName(vendorId, customerName);
            return ResponseEntity.ok(leads);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/duplicates")
    public ResponseEntity<List<Lead>> findDuplicateLeads(
            @RequestParam String email, 
            @RequestParam String phone) {
        try {
            List<Lead> duplicates = leadService.findDuplicateLeads(email, phone);
            return ResponseEntity.ok(duplicates);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/statuses")
    public ResponseEntity<LeadStatus[]> getLeadStatuses() {
        return ResponseEntity.ok(LeadStatus.values());
    }

    @GetMapping("/priorities")
    public ResponseEntity<LeadPriority[]> getLeadPriorities() {
        return ResponseEntity.ok(LeadPriority.values());
    }
}
