package com.akademi.finsight.bootstrap;


import com.akademi.finsight.user.entity.Role;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.repository.UserRepository;
import com.akademi.finsight.user.util.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("dev")
@EnableConfigurationProperties(AdminProperties.class)
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Override
    public void run(String... args) {
        String normalizedEmail = EmailNormalizer.normalize(adminProperties.getEmail());

        if (userRepository.existsByEmail(normalizedEmail)
                || userRepository.existsByUsername(adminProperties.getUsername())) {
            log.info("Admin seed skipped: event=ADMIN_SEED_SKIPPED, reason=ALREADY_EXISTS");
            return;
        }

        User admin = User.builder()
                .email(normalizedEmail)
                .username(adminProperties.getUsername())
                .password(passwordEncoder.encode(adminProperties.getPassword()))
                .firstName(adminProperties.getFirstName())
                .lastName(adminProperties.getLastName())
                .phoneNumber(adminProperties.getPhoneNumber())
                .role(Role.ADMIN)
                .enabled(true)
                .firstLogin(true)
                .emailVerified(true)
                .build();

        userRepository.save(admin);
        log.info("Admin seed completed: event=ADMIN_SEEDED");
    }
}
