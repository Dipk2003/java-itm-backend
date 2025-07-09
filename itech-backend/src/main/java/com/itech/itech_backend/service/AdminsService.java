package com.itech.itech_backend.service;

import com.itech.itech_backend.model.Admins;
import com.itech.itech_backend.repository.AdminsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminsService {

    private final AdminsRepository adminsRepository;

    public List<Admins> getAllAdmins() {
        return adminsRepository.findAll();
    }

    public Optional<Admins> getAdminById(Long id) {
        return adminsRepository.findById(id);
    }

    public Optional<Admins> getAdminByEmail(String email) {
        return adminsRepository.findByEmail(email);
    }

    public Optional<Admins> getAdminByPhone(String phone) {
        return adminsRepository.findByPhone(phone);
    }

    public Optional<Admins> getActiveAdminByEmail(String email) {
        return adminsRepository.findByEmailAndIsActiveTrue(email);
    }

    public Admins saveAdmin(Admins admin) {
        return adminsRepository.save(admin);
    }

    public void deleteAdmin(Long id) {
        adminsRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return adminsRepository.existsByEmail(email);
    }

    public boolean existsByPhone(String phone) {
        return adminsRepository.existsByPhone(phone);
    }

    public long getAdminCount() {
        return adminsRepository.count();
    }

    public List<Admins> getActiveAdmins() {
        return adminsRepository.findAll().stream()
                .filter(Admins::isActive)
                .toList();
    }

    public List<Admins> getInactiveAdmins() {
        return adminsRepository.findAll().stream()
                .filter(admin -> !admin.isActive())
                .toList();
    }

    public List<Admins> getVerifiedAdmins() {
        return adminsRepository.findAll().stream()
                .filter(Admins::isVerified)
                .toList();
    }

    public List<Admins> getUnverifiedAdmins() {
        return adminsRepository.findAll().stream()
                .filter(admin -> !admin.isVerified())
                .toList();
    }

    public List<Admins> getAdminsByDepartment(String department) {
        return adminsRepository.findAll().stream()
                .filter(admin -> admin.getDepartment() != null && admin.getDepartment().equals(department))
                .toList();
    }

    public void deactivateAdmin(Long id) {
        Optional<Admins> adminOpt = adminsRepository.findById(id);
        if (adminOpt.isPresent()) {
            Admins admin = adminOpt.get();
            admin.setActive(false);
            adminsRepository.save(admin);
        }
    }

    public void activateAdmin(Long id) {
        Optional<Admins> adminOpt = adminsRepository.findById(id);
        if (adminOpt.isPresent()) {
            Admins admin = adminOpt.get();
            admin.setActive(true);
            adminsRepository.save(admin);
        }
    }
}
