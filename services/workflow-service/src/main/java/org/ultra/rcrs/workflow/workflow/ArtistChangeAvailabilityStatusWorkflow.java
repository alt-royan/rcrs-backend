package org.ultra.rcrs.workflow.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.springframework.http.ResponseEntity;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.workflow.dto.ChangeAvailabilityStatusRequest;
import org.ultra.rcrs.workflow.dto.CreateResponse;
import org.ultra.rcrs.workflow.dto.RegisterArtistRequest;

@WorkflowInterface
public interface ArtistChangeAvailabilityStatusWorkflow {

    @WorkflowMethod
    ResponseEntity<Void> changeAvailabilityStatus(EntityStatus status, String id);
}
