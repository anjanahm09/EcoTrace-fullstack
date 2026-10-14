
package com.ecotrace.config;

import com.ecotrace.entity.Organization;
import com.ecotrace.entity.UserAccount;
import com.ecotrace.repository.OrganizationRepository;
import com.ecotrace.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoUserSeeder {

    @Bean
    CommandLineRunner seedDemoUsers(
            UserAccountRepository userRepository,
            OrganizationRepository organizationRepository,
            PasswordEncoder passwordEncoder,
            @Value("${ECOTRACE_ADMIN_PASSWORD:ChangeThisAdmin123!}")
            String adminPassword,
            @Value("${ECOTRACE_STAKEHOLDER_PASSWORD:ChangeThisStakeholder123!}")
            String stakeholderPassword,
            @Value("${ECOTRACE_AUDITOR_PASSWORD:ChangeThisAuditor123!}")
            String auditorPassword) {

        return args -> {
            // Create a demo organization if the database is empty.
            Organization organization;

            if (organizationRepository.count() == 0) {
                organization = new Organization();
                organization.setName("EcoTrace Demo Organization");
                organization.setOrganizationType("MANUFACTURER");
                organization.setCountry("India");
                organization.setBoundaryDescription(
                        "Demo reporting organization for EcoTrace"
                );

                organization = organizationRepository.save(organization);
            } else {
                organization = organizationRepository.findAll().get(0);
            }

            createUserIfMissing(
                    userRepository,
                    passwordEncoder,
                    "admin",
                    adminPassword,
                    "ADMIN",
                    null
            );

            createUserIfMissing(
                    userRepository,
                    passwordEncoder,
                    "stakeholder",
                    stakeholderPassword,
                    "STAKEHOLDER",
                    organization.getId()
            );

            createUserIfMissing(
                    userRepository,
                    passwordEncoder,
                    "auditor",
                    auditorPassword,
                    "AUDITOR",
                    null
            );
        };
    }

    private void createUserIfMissing(
            UserAccountRepository repository,
            PasswordEncoder passwordEncoder,
            String username,
            String rawPassword,
            String role,
            Long organizationId) {

        if (repository.existsByUsernameIgnoreCase(username)) {
            return;
        }

        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(passwordEncoder.encode(rawPassword));
        account.setRole(role);
        account.setOrganizationId(organizationId);
        account.setEnabled(true);

        repository.save(account);
    }
}
