package org.ultra.rcrs.workflow.handler;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.workflow.dto.CreateResponse;
import org.ultra.rcrs.workflow.dto.RegisterArtistRequest;
import org.ultra.rcrs.workflow.workflow.ArtistRegistrationWorkflow;

import java.util.UUID;

import static org.ultra.rcrs.workflow.config.TemporalConfig.WORKFLOW_TASK_QUEUE;

@Service
@RequiredArgsConstructor
public class WorkflowHandler {

    private final WorkflowClient workflowClient;

    public CreateResponse startRegisterArtistWorkflow(RegisterArtistRequest request) {

        ArtistRegistrationWorkflow workflow = workflowClient.newWorkflowStub(
                ArtistRegistrationWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(WORKFLOW_TASK_QUEUE)
                        .setWorkflowId(UUID.randomUUID().toString())
                        .build()
        );
        var future = WorkflowClient.execute(workflow::registerArtist, request);

        return future.join();
    }

}
