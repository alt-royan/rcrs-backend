package org.ultra.rcrs.workflow.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflow.handler.WorkflowHandler;

@Component
@RequiredArgsConstructor
@Slf4j
public class PurgeWorkflowScheduler implements CommandLineRunner {

    private final WorkflowHandler workflowHandler;

    @Override
    public void run(String... args) {
        try {
            workflowHandler.startPurgeDeletedWorkflow();
            log.info("Scheduled monthly purge workflow (cron: 0 0 4 1 * *)");
        } catch (Exception e) {
            log.warn("Could not schedule purge workflow (may already exist): {}", e.getMessage());
        }
    }
}
