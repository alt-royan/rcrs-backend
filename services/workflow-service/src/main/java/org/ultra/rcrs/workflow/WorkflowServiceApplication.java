package org.ultra.rcrs.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.ultra.rcrs.workflow.config.TemporalProperties;

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties({TemporalProperties.class})
public class WorkflowServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowServiceApplication.class, args);
    }
}
