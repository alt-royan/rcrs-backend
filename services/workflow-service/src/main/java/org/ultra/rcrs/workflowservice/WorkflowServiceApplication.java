package org.ultra.rcrs.workflowservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.ultra.rcrs.workflowservice.config.temporal.ServiceProperties;
import org.ultra.rcrs.workflowservice.config.temporal.TemporalProperties;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties({TemporalProperties.class, ServiceProperties.class})
public class WorkflowServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowServiceApplication.class, args);
    }
}
