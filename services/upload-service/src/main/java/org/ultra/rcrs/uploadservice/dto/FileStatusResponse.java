package org.ultra.rcrs.uploadservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.ultra.rcrs.enums.FileStatus;

@Data
public class FileStatusResponse {

    private String uid;
    private FileStatus status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String reason;

}
