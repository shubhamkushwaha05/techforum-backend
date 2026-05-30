package com.techforum.config;

import com.techforum.entity.Role;
import com.techforum.enums.RoleName;
import com.techforum.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    // FIX: Use proper SLF4J logger instead of System.out.println.
    //      System.out bypasses the logging framework (no timestamps, no log levels,
    //      no filtering, no file appenders) and pollutes stdout in production.
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        int seeded = 0;
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(null, roleName));
                seeded++;
                log.info("Seeded role: {}", roleName);
            }
        }
        if (seeded == 0) {
            log.debug("All roles already present — nothing to seed");
        } else {
            log.info("DataSeeder complete — {} role(s) inserted", seeded);
        }
    }
}
