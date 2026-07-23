package org.ultra.rcrs.mediaservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranscodingWorkflowInput {

    private String uid;
    private String trackId;
}