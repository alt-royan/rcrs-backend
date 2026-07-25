package org.ultra.rcrs.playlistservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "playlists")
public class PlaylistDocument {

    @Id
    private String id;
    private String ownerId;
    private String title;
    private String description;
    private List<String> tags;
    private String coverS3Key;
    private Boolean isPublic;
    private Integer trackCount;
    private List<PlaylistTrack> tracks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
