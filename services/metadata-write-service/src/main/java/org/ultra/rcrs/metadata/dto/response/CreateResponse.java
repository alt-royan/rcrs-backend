package org.ultra.rcrs.metadata.dto.response;

import lombok.Data;
import org.ultra.rcrs.utils.Url62;

import java.util.UUID;

@Data
public class CreateResponse {

    private String id;

    public CreateResponse(UUID uuid) {
        this.id = Url62.encode(uuid);
    }
}
