package org.ultra.rcrs.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.userservice.dto.IdentityEvent;
import org.ultra.rcrs.userservice.dto.IdentityEventPayload;
import org.ultra.rcrs.userservice.dto.IdentityEventType;
import org.ultra.rcrs.userservice.model.User;
import org.ultra.rcrs.userservice.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void handleEvent(IdentityEvent event) {
        switch (event.getEventType()) {
            case USER_CREATED -> handleCreated(event.getPayload());
            case USER_UPDATED -> handleUpdated(event.getPayload());
            case USER_DELETED -> handleDeleted(event.getPayload());
        }
    }

    private void handleCreated(IdentityEventPayload payload) {
        Optional<User> existing = userRepository.findByKeycloakId(payload.getKeycloakId());

        if (existing.isPresent()) {
            User user = existing.get();
            user.setUsername(payload.getUsername());
            user.setEmail(payload.getEmail());
            user.setFirstName(payload.getFirstName());
            user.setLastName(payload.getLastName());
            user.setEnabled(payload.isEnabled());
            user.setEmailVerified(payload.isEmailVerified());
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
            log.info("Updated existing user on USER_CREATED: keycloakId={}", payload.getKeycloakId());
        } else {
            Instant now = Instant.now();
            User user = User.builder()
                    .keycloakId(payload.getKeycloakId())
                    .username(payload.getUsername())
                    .email(payload.getEmail())
                    .firstName(payload.getFirstName())
                    .lastName(payload.getLastName())
                    .enabled(payload.isEnabled())
                    .emailVerified(payload.isEmailVerified())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            userRepository.save(user);
            log.info("Created new user: keycloakId={} username={}", payload.getKeycloakId(), payload.getUsername());
        }
    }

    private void handleUpdated(IdentityEventPayload payload) {
        userRepository.findByKeycloakId(payload.getKeycloakId()).ifPresentOrElse(user -> {
            user.setUsername(payload.getUsername());
            user.setEmail(payload.getEmail());
            user.setFirstName(payload.getFirstName());
            user.setLastName(payload.getLastName());
            user.setEmailVerified(payload.isEmailVerified());
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
            log.info("Updated user profile: keycloakId={}", payload.getKeycloakId());
        }, () -> log.warn("USER_UPDATED for unknown keycloakId={}, ignoring", payload.getKeycloakId()));
    }

    private void handleDeleted(IdentityEventPayload payload) {
        userRepository.findByKeycloakId(payload.getKeycloakId()).ifPresentOrElse(user -> {
            user.setEnabled(false);
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
            log.info("Soft-deleted user: keycloakId={}", payload.getKeycloakId());
        }, () -> log.warn("USER_DELETED for unknown keycloakId={}, ignoring", payload.getKeycloakId()));
    }
}
