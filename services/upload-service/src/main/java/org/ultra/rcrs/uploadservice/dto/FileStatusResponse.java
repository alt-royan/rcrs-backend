package org.ultra.rcrs.uploadservice.dto;

import lombok.Data;
import org.ultra.rcrs.enums.FileStatus;

@Data
public class FileStatusResponse {

    private FileStatus status;
    private String reason;

}
