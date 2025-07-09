package com.itech.itech_backend.controller;

import com.itech.itech_backend.model.Admins;
import com.itech.itech_backend.service.AdminsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admins")
@CrossOrigin
@RequiredArgsConstructor
public class AdminsController {

    private final AdminsService adminsService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Admins>> getAllAdmins() {
        List<Admins> admins = adminsService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Admins> getAdminById(@PathVariable Long id) {
        Optional<Admins> admin = adminsService.getAdminById(id);
        return admin.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Admins> getAdminByEmail(@PathVariable String email) {
        Optional<Admins> admin = adminsService.getAdminByEmail(email);
        return admin.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/phone/{phone}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Admins> getAdminByPhone(@PathVariable String phone) {
        Optional<Admins> admin = adminsService.getAdminByPhone(phone);
        return admin.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Admins>> getActiveAdmins() {
        List<Admins> admins = adminsService.getActiveAdmins();
        return ResponseEntity.ok(admins);
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Admins>> getInactiveAdmins() {
        List<Admins> admins = adminsService.getInactiveAdmins();
        return ResponseEntity.ok(admins);
    }

    @GetMapping("/verified")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Admins>> getVerifiedAdmins() {
        List<Admins> admins = adminsService.getVerifiedAdmins();
        return ResponseEntity.ok(admins);
    }

    @GetMapping("/unverified")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Admins>> getUnverifiedAdmins() {
        List<Admins> admins = adminsService.getUnverifiedAdmins();
        return ResponseEntity.ok(admins);
    }

    @GetMapping("/department/{department}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Admins>> getAdminsByDepartment(@PathVariable String department) {
        List<Admins> admins = adminsService.getAdminsByDepartment(department);
        return ResponseEntity.ok(admins);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Admins> updateAdmin(@PathVariable Long id, @RequestBody Admins admin) {
        Optional<Admins> existingAdmin = adminsService.getAdminById(id);
        if (existingAdmin.isPresent()) {
            admin.setId(id);
            Admins updatedAdmin = adminsService.saveAdmin(admin);
            return ResponseEntity.ok(updatedAdmin);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        if (adminsService.getAdminById(id).isPresent()) {
            adminsService.deleteAdmin(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateAdmin(@PathVariable Long id) {
        if (adminsService.getAdminById(id).isPresent()) {
            adminsService.activateAdmin(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateAdmin(@PathVariable Long id) {
        if (adminsService.getAdminById(id).isPresent()) {
            adminsService.deactivateAdmin(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getAdminCount() {
        long count = adminsService.getAdminCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/exists/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = adminsService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists/phone/{phone}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> existsByPhone(@PathVariable String phone) {
        boolean exists = adminsService.existsByPhone(phone);
        return ResponseEntity.ok(exists);
    }
}
