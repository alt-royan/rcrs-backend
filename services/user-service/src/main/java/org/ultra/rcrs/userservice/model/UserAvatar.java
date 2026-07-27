package org.ultra.rcrs.userservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

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
    private String userId;

    @Column(name = "avatar_key", nullable = false)
    private String avatarKey;
}
