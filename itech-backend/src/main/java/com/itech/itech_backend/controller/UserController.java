package com.itech.itech_backend.controller;

import com.itech.itech_backend.model.User;
import com.itech.itech_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Get all users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Get users by role
    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role) {
        List<User> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    // Get user by email
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.getUserByEmail(email);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    // Get user by phone
    @GetMapping("/phone/{phone}")
    public ResponseEntity<User> getUserByPhone(@PathVariable String phone) {
        Optional<User> user = userService.getUserByPhone(phone);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    // Get verified users
    @GetMapping("/verified")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getVerifiedUsers() {
        List<User> users = userService.getVerifiedUsers();
        return ResponseEntity.ok(users);
    }

    // Get unverified users
    @GetMapping("/unverified")
    public ResponseEntity<List<User>> getUnverifiedUsers() {
        List<User> users = userService.getUnverifiedUsers();
        return ResponseEntity.ok(users);
    }

    // Get active users
    @GetMapping("/active")
    public ResponseEntity<List<User>> getActiveUsers() {
        List<User> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }

    // Get inactive users
    @GetMapping("/inactive")
    public ResponseEntity<List<User>> getInactiveUsers() {
        List<User> users = userService.getInactiveUsers();
        return ResponseEntity.ok(users);
    }

    // Update user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(id, userDetails);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete user
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok("User deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }

    // Activate user
    @PatchMapping("/{id}/activate")
    public ResponseEntity<String> activateUser(@PathVariable Long id) {
        boolean activated = userService.activateUser(id);
        if (activated) {
            return ResponseEntity.ok("User activated successfully");
        }
        return ResponseEntity.notFound().build();
    }

    // Deactivate user
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable Long id) {
        boolean deactivated = userService.deactivateUser(id);
        if (deactivated) {
            return ResponseEntity.ok("User deactivated successfully");
        }
        return ResponseEntity.notFound().build();
    }

    // Get user count
    @GetMapping("/count")
    public ResponseEntity<Long> getUserCount() {
        long count = userService.getUserCount();
        return ResponseEntity.ok(count);
    }

    // Get user count by role
    @GetMapping("/count/role/{role}")
    public ResponseEntity<Long> getUserCountByRole(@PathVariable String role) {
        long count = userService.getUserCountByRole(role);
        return ResponseEntity.ok(count);
    }

    // Check if user exists by email
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    // Check if user exists by phone
    @GetMapping("/exists/phone/{phone}")
    public ResponseEntity<Boolean> existsByPhone(@PathVariable String phone) {
        boolean exists = userService.existsByPhone(phone);
        return ResponseEntity.ok(exists);
    }

    // Get all regular users (ROLE_USER)
    @GetMapping("/regular")
    public ResponseEntity<List<User>> getRegularUsers() {
        List<User> users = userService.getUsersByRole("ROLE_USER");
        return ResponseEntity.ok(users);
    }

    // Get all vendors (ROLE_VENDOR)
    @GetMapping("/vendors")
    public ResponseEntity<List<User>> getVendors() {
        List<User> users = userService.getUsersByRole("ROLE_VENDOR");
        return ResponseEntity.ok(users);
    }

    // Get all admins (ROLE_ADMIN)
    @GetMapping("/admins")
    public ResponseEntity<List<User>> getAdmins() {
        List<User> users = userService.getUsersByRole("ROLE_ADMIN");
        return ResponseEntity.ok(users);
    }
}
