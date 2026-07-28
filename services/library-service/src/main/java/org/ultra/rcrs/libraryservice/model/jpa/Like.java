package org.ultra.rcrs.libraryservice.model.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "likes")
@IdClass(LikePK.class)
public class Like {

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private LibraryEntityType entityType;

    @Id
    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "liked_at", nullable = false)
    private Instant likedAt;
}
