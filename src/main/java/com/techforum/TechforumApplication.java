package com.techforum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application entry point.
 *
 * FIX: There were TWO TechforumApplication classes in the project:
 *   1. com.techforum.TechforumApplication          (this file — correct)
 *   2. com.techforum.techforum.TechforumApplication (duplicate — DELETE IT)
 *
 * The duplicate in the sub-package com.techforum.techforum causes Spring Boot's
 * component scan to potentially register beans twice and makes the test class
 * reference the wrong package. Delete the file at:
 *   src/main/java/com/techforum/techforum/TechforumApplication.java
 *
 * The test class TechforumApplicationTests in com.techforum.techforum should
 * also be updated to import this class (or moved to com.techforum package).
 */
@SpringBootApplication
public class TechforumApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechforumApplication.class, args);
    }
}
