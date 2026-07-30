package org.ultra.rcrs.libraryservice.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayCountId implements Serializable {

    private String userId;

    private String trackId;
}
