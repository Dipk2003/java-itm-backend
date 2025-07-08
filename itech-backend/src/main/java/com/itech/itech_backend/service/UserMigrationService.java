package com.itech.itech_backend.service;

import com.itech.itech_backend.model.User;
import com.itech.itech_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserMigrationService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        migrateUsersWithoutPasswords();
    }

    public void migrateUsersWithoutPasswords() {
        try {
            List<User> usersWithoutPassword = userRepository.findAll().stream()
                    .filter(user -> user.getPassword() == null || user.getPassword().isEmpty())
                    .toList();

            if (!usersWithoutPassword.isEmpty()) {
                log.info("🔄 Found {} users without passwords. Setting default passwords...", usersWithoutPassword.size());

                for (User user : usersWithoutPassword) {
                    // Set a default password (user will need to change it)
                    String defaultPassword = "password123"; // This should be changed by user
                    user.setPassword(passwordEncoder.encode(defaultPassword));
                    userRepository.save(user);
                    
                    log.info("✅ Set default password for user: {} ({})", user.getName(), user.getEmail());
                    System.out.println("🔑 USER: " + user.getEmail() + " | DEFAULT PASSWORD: " + defaultPassword);
                }

                System.out.println("\n" + "=".repeat(80));
                System.out.println("🔄 USER MIGRATION COMPLETED");
                System.out.println("📋 " + usersWithoutPassword.size() + " users updated with default passwords");
                System.out.println("🔑 DEFAULT PASSWORD FOR ALL EXISTING USERS: password123");
                System.out.println("⚠️  SECURITY NOTICE: Users should change their passwords after first login");
                System.out.println("=".repeat(80) + "\n");
            } else {
                log.info("✅ All users already have passwords set");
            }
        } catch (Exception e) {
            log.error("❌ Error during user migration: {}", e.getMessage(), e);
        }
    }
}
