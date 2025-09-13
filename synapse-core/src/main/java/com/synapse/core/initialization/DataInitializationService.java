package com.synapse.core.initialization;

import com.synapse.data.entity.Role;
import com.synapse.data.entity.User;
import com.synapse.data.repository.jpa.RoleRepository;
import com.synapse.data.repository.jpa.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

/**
 * Service for initializing system data on first startup.
 */
@Service
@Transactional
public class DataInitializationService {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializationService.class);
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@synapse.local";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private boolean initialized = false;

    /**
     * Initialize system data when application is ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeSystemData() {
        if (initialized) {
            return;
        }

        logger.info("Starting system initialization...");

        try {
            createDefaultAdminUser();
            initialized = true;
            logger.info("System initialization completed successfully");
        } catch (Exception e) {
            logger.error("System initialization failed", e);
        }
    }

    /**
     * Create default SYSTEM_ADMIN user if none exists.
     */
    private void createDefaultAdminUser() {
        // Check if any SYSTEM_ADMIN user exists
        if (!userRepository.findByRoleNameAndNotDeleted(Role.SYSTEM_ADMIN).isEmpty()) {
            logger.info("SYSTEM_ADMIN user already exists, skipping creation");
            return;
        }

        // Check if default admin username exists
        if (userRepository.existsByUsernameAndNotDeleted(DEFAULT_ADMIN_USERNAME)) {
            logger.info("Default admin username already exists, skipping creation");
            return;
        }

        // Generate secure random password
        String password = generateSecurePassword();
        String hashedPassword = passwordEncoder.encode(password);

        // Create admin user
        User adminUser = new User(DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_EMAIL, hashedPassword);

        // Assign SYSTEM_ADMIN role
        Optional<Role> systemAdminRole = roleRepository.findByName(Role.SYSTEM_ADMIN);
        if (systemAdminRole.isPresent()) {
            adminUser.addRole(systemAdminRole.get());
        } else {
            logger.error("SYSTEM_ADMIN role not found in database");
            return;
        }

        userRepository.save(adminUser);

        logger.warn("=".repeat(80));
        logger.warn("DEFAULT ADMIN USER CREATED");
        logger.warn("Username: {}", DEFAULT_ADMIN_USERNAME);
        logger.warn("Password: {}", password);
        logger.warn("Email: {}", DEFAULT_ADMIN_EMAIL);
        logger.warn("PLEASE CHANGE THE PASSWORD IMMEDIATELY AFTER FIRST LOGIN");
        logger.warn("=".repeat(80));
    }

    /**
     * Generate a secure random password.
     */
    private String generateSecurePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    /**
     * Check if system has been initialized.
     */
    public boolean isInitialized() {
        return initialized || !userRepository.findByRoleNameAndNotDeleted(Role.SYSTEM_ADMIN).isEmpty();
    }

    /**
     * Get setup completion status.
     */
    public SetupStatus getSetupStatus() {
        boolean hasAdminUser = !userRepository.findByRoleNameAndNotDeleted(Role.SYSTEM_ADMIN).isEmpty();
        
        if (!hasAdminUser) {
            return new SetupStatus(false, "No SYSTEM_ADMIN user found");
        }

        return new SetupStatus(true, "System setup completed");
    }

    /**
     * Setup status DTO.
     */
    public static class SetupStatus {
        private boolean completed;
        private String message;

        public SetupStatus(boolean completed, String message) {
            this.completed = completed;
            this.message = message;
        }

        public boolean isCompleted() { return completed; }
        public String getMessage() { return message; }
    }
}