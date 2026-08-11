package com.akademi.finsight.common.migration;

import com.akademi.finsight.user.config.AdminProperties;
import com.akademi.finsight.user.entity.Role;
import com.akademi.finsight.user.util.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Seeds the initial ADMIN account from the {@code ADMIN_*} environment variables.
 * Java-based migration because the password must be BCrypt hashed with the same
 * {@link PasswordEncoder} the login flow uses, which plain SQL cannot do.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(AdminProperties.class)
public class V28__Seed_admin_user extends BaseJavaMigration {

    private static final String COUNT_EXISTING_ADMIN = """
            SELECT COUNT(1) FROM users WHERE email = ? OR username = ?
            """;

    private static final String INSERT_ADMIN = """
            INSERT INTO users (email, username, password, first_name, last_name, phone_number,
                               role, enabled, first_login, email_verified)
            VALUES (?, ?, ?, ?, ?, ?, ?, 1, 1, 1)
            """;

    private final AdminProperties adminProperties;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void migrate(Context context) throws SQLException {
        Connection connection = context.getConnection();
        String email = EmailNormalizer.normalize(adminProperties.getEmail());
        String username = adminProperties.getUsername();

        if (adminExists(connection, email, username)) {
            log.info("Admin seed skipped: event=ADMIN_SEED_SKIPPED, reason=ALREADY_EXISTS");
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement(INSERT_ADMIN)) {
            statement.setString(1, email);
            statement.setString(2, username);
            statement.setString(3, passwordEncoder.encode(adminProperties.getPassword()));
            statement.setString(4, adminProperties.getFirstName());
            statement.setString(5, adminProperties.getLastName());
            statement.setString(6, adminProperties.getPhoneNumber());
            statement.setString(7, Role.ADMIN.name());
            statement.executeUpdate();
        }

        log.info("Admin seed completed: event=ADMIN_SEEDED");
    }

    private boolean adminExists(Connection connection, String email, String username) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(COUNT_EXISTING_ADMIN)) {
            statement.setString(1, email);
            statement.setString(2, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }
}
