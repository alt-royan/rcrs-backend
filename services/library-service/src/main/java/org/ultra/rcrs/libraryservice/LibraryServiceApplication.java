package org.ultra.rcrs.libraryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * This service talks to two stores, so the two Spring Data modules must be told
 * explicitly which packages they own. Left to auto-configuration they both try to
 * claim every repository interface they find and the context fails to start.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = "org.ultra.rcrs.libraryservice.repository.jpa")
@EnableMongoRepositories(basePackages = "org.ultra.rcrs.libraryservice.repository.mongo")
public class LibraryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryServiceApplication.class, args);
    }
}
