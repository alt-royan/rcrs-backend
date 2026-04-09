package org.ultra.rcrs.mediaservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.ultra.rcrs.enums.FileStatus;

@Data
@AllArgsConstructor
public class FileStatusResponse {

    private String uid;

    private FileStatus status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String reason;
}
