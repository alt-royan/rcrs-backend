package org.ultra.rcrs.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.userservice.model.UserAvatar;

@Repository
public interface UserAvatarRepository extends JpaRepository<UserAvatar, String> {
}
