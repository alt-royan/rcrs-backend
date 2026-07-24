package org.ultra.rcrs.userservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_avatar")
public class UserAvatar {

    @Id
    @Column(name = "user_id", nullable = false)
    private java.util.UUID userId;

    @Column(name = "avatar_key", nullable = false)
    private String avatarKey;
}
