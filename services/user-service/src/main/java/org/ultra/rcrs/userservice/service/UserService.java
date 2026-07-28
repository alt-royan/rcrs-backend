package org.ultra.rcrs.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.enums.ImageSize;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.userservice.dto.IdentityEvent;
import org.ultra.rcrs.userservice.dto.IdentityEventPayload;
import org.ultra.rcrs.userservice.dto.UserProfileResponse;
import org.ultra.rcrs.userservice.model.User;
import org.ultra.rcrs.userservice.model.UserAvatar;
import org.ultra.rcrs.userservice.repository.UserAvatarRepository;
import org.ultra.rcrs.userservice.repository.UserRepository;
import org.ultra.rcrs.utils.ImageUtils;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserAvatarRepository userAvatarRepository;
    private final ImageUtils imageUtils;

    @Transactional
    public void handleEvent(IdentityEvent event) {
        switch (event.getEventType()) {
            case USER_CREATED -> handleCreated(event.getPayload());
            case USER_UPDATED -> handleUpdated(event.getPayload());
            case USER_DELETED -> handleDeleted(event.getPayload());
        }
    }

    private void handleCreated(IdentityEventPayload payload) {
        Optional<User> existing = userRepository.findByUserId(payload.getUserId());

        if (existing.isPresent()) {
            User user = existing.get();
            user.setUsername(payload.getUsername());
            user.setEmail(payload.getEmail());
            user.setEnabled(payload.isEnabled());
            user.setEmailVerified(payload.isEmailVerified());
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
            log.info("Updated existing user on USER_CREATED: userId={}", payload.getUserId());
        } else {
            Instant now = Instant.now();
            User user = User.builder()
                    .userId(payload.getUserId())
                    .username(payload.getUsername())
                    .email(payload.getEmail())
                    .enabled(payload.isEnabled())
                    .emailVerified(payload.isEmailVerified())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            userRepository.save(user);
            log.info("Created new user: userId={} username={}", payload.getUserId(), payload.getUsername());
        }
    }

    private void handleUpdated(IdentityEventPayload payload) {
        userRepository.findByUserId(payload.getUserId()).ifPresentOrElse(user -> {
            user.setUsername(payload.getUsername());
            user.setEmail(payload.getEmail());
            user.setEmailVerified(payload.isEmailVerified());
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
            log.info("Updated user profile: userId={}", payload.getUserId());
        }, () -> log.warn("USER_UPDATED for unknown userId={}, ignoring", payload.getUserId()));
    }

    private void handleDeleted(IdentityEventPayload payload) {
        userRepository.findByUserId(payload.getUserId()).ifPresentOrElse(user -> {
            user.setEnabled(false);
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
            log.info("Soft-deleted user: userId={}", payload.getUserId());
        }, () -> log.warn("USER_DELETED for unknown userId={}, ignoring", payload.getUserId()));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        Map<ImageSize, URI> avatar = getAvatarUrls(user.getUserId());

        return new UserProfileResponse(
                user.getUserId(),
                user.getUsername(),
                avatar,
                user.getEmail(),
                user.isEnabled(),
                user.isEmailVerified()
        );
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCompactProfile(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        Map<ImageSize, URI> avatar = getAvatarUrls(user.getUserId());

        return new UserProfileResponse(
                user.getUserId(),
                user.getUsername(),
                avatar,
                null,
                null,
                null
        );
    }

    private Map<ImageSize, URI> getAvatarUrls(String userId) {
        return userAvatarRepository.findById(userId)
                .map(UserAvatar::getAvatarKey)
                .map(imageUtils::parseUrls)
                .orElse(Map.of());
    }
}
