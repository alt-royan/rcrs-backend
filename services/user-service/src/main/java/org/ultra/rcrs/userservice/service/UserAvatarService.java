package org.ultra.rcrs.userservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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

        UserAvatar avatar = userAvatarRepository.findById(user.getId())
                .map(existing -> {
                    existing.setAvatarKey(avatarKey);
                    return existing;
                })
                .orElseGet(() -> UserAvatar.builder()
                        .userId(user.getId())
                        .avatarKey(avatarKey)
                        .build());

        userAvatarRepository.save(avatar);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String username, boolean full) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found for username: " + username));

        String avatarKey = userAvatarRepository.findById(user.getId())
                .map(UserAvatar::getAvatarKey)
                .orElse(null);

        if (full) {
            return new UserProfileResponse(
                    user.getUsername(),
                    avatarKey,
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.isEnabled(),
                    user.isEmailVerified()
            );
        }

        return new UserProfileResponse(
                user.getUsername(),
                avatarKey,
                null,
                null,
                null,
                null,
                null
        );
    }
}
