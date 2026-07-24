package org.ultra.rcrs.metadata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MetadataWriteServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetadataWriteServiceApplication.class, args);
    }

}
