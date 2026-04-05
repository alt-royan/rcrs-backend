package org.ultra.rcrs.catalogservice.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@JsonPropertyOrder({"status", "message"})
public class ErrorResponse {

    private int status;
    private String message;

}