package org.ultra.rcrs.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.userservice.dto.IdentityEvent;
import org.ultra.rcrs.userservice.dto.IdentityEventPayload;
import org.ultra.rcrs.userservice.dto.MeResponse;
import org.ultra.rcrs.userservice.model.User;
import org.ultra.rcrs.userservice.repository.UserRepository;
import org.ultra.rcrs.utils.ImageUtils;
import org.ultra.rcrs.utils.Url62;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ImageUtils imageUtils;

    @Transactional
    public void handleEvent(IdentityEvent event) {
        switch (event.getEventType()) {
            case USER_CREATED -> handleCreated(event.getPayload());
            case USER_UPDATED -> handleUpdated(event.getPayload());
        }
    }

    private void handleCreated(IdentityEventPayload payload) {
        Optional<User> existing = userRepository.findById(UUID.fromString(payload.getUserId()));

        if (existing.isPresent()) {
            User user = existing.get();
            user.setUsername(payload.getUsername());
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
            log.info("Updated existing user on USER_CREATED: userId={}", payload.getUserId());
        } else {
            Instant now = Instant.now();
            User user = User.builder()
                    .id(UUID.fromString(payload.getUserId()))
                    .username(payload.getUsername())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            userRepository.save(user);
            log.info("Created new user: userId={} username={}", payload.getUserId(), payload.getUsername());
        }
    }

    private void handleUpdated(IdentityEventPayload payload) {
        userRepository.findById(UUID.fromString(payload.getUserId()))
                .ifPresentOrElse(user -> {
                    user.setUsername(payload.getUsername());
                    user.setUpdatedAt(Instant.now());
                    userRepository.save(user);
                    log.info("Updated user profile: userId={}", payload.getUserId());
                }, () -> log.warn("USER_UPDATED for unknown userId={}, ignoring", payload.getUserId()));
    }

    @Transactional(readOnly = true)
    public MeResponse getMe(Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String userAvatar = userRepository.findById(userId)
                .map(User::getAvatarKey)
                .orElse(null);

        return new MeResponse(
                Url62.encode(userId),
                jwt.getClaimAsString("preferred_username"),
                imageUtils.parseUrls(userAvatar)
        );
    }

    @Transactional
    public void saveAvatar(Jwt jwt, String avatarUri) {
        UUID userId = UUID.fromString(jwt.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found for id: " + userId));

        String avatarKey = imageUtils.parseKey(avatarUri);

        user.setAvatarKey(avatarKey);

        userRepository.save(user);
    }
}
