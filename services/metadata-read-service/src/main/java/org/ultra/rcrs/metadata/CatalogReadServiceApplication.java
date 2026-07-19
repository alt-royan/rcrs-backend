package org.ultra.rcrs.metadata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CatalogReadServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogReadServiceApplication.class, args);
    }

}
