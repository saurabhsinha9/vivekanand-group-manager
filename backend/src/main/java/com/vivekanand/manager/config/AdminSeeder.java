package com.vivekanand.manager.config;

import com.vivekanand.manager.auth.Role;
import com.vivekanand.manager.auth.User;
import com.vivekanand.manager.auth.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner seedAdmin(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("saurabhsinha9@gmail.com");
                admin.setRole(Role.ADMIN);
                admin.setPasswordHash(encoder.encode("admin")); // change in prod
                repo.save(admin);
                System.out.println(">> Seeded default ADMIN user: admin / admin");
            }
        };
    }
}
