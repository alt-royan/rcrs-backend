package org.ultra.rcrs.userservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.userservice.dto.UserProfileResponse;
import org.ultra.rcrs.userservice.model.User;
import org.ultra.rcrs.userservice.model.UserAvatar;
import org.ultra.rcrs.userservice.repository.UserAvatarRepository;
import org.ultra.rcrs.userservice.repository.UserRepository;
import org.ultra.rcrs.utils.S3Utils;

@Service
@RequiredArgsConstructor
public class UserAvatarService {

    private final UserRepository userRepository;
    private final UserAvatarRepository userAvatarRepository;
    private final S3Utils s3Utils;

    @Transactional
    public void saveAvatar(String username, String avatarUri) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found for username: " + username));

        String avatarKey = s3Utils.parseKey(avatarUri);

        UserAvatar avatar = userAvatarRepository.findById(user.getUserId())
                .map(existing -> {
                    existing.setAvatarKey(avatarKey);
                    return existing;
                })
                .orElseGet(() -> UserAvatar.builder()
                        .userId(user.getUserId())
                        .avatarKey(avatarKey)
                        .build());

        userAvatarRepository.save(avatar);
    }
}
