package com.menstyle.config;

import com.menstyle.entity.Role;
import com.menstyle.entity.User;
import com.menstyle.entity.enums.UserStatus;
import com.menstyle.repository.RoleRepository;
import com.menstyle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class AdminDataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String fullName;

    public AdminDataInitializer(RoleRepository roleRepository, UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${ADMIN_EMAIL:}") String email,
                                @Value("${ADMIN_PASSWORD:}") String password,
                                @Value("${ADMIN_FULL_NAME:MENSTYLE Administrator}") String fullName) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email.trim().toLowerCase(java.util.Locale.ROOT);
        this.password = password;
        this.fullName = fullName;
    }

    @Override
    public void run(String... args) {
        if (email.isBlank() || password.isBlank()) {
            return;
        }
        if (userRepository.existsByEmail(email)) {
            return;
        }
        Role role = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("Admin role is missing"));
        User admin = new User();
        admin.setEmail(email);
        admin.setFullName(fullName);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(role);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setEmailVerified(true);
        userRepository.save(admin);
    }
}
