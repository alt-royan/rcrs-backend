package org.ultra.rcrs.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    public void handleRegistration(String keycloakId, String username, String email, String firstName, String lastName) {
        Optional<User> existing = userRepository.findByKeycloakId(keycloakId);

        if (existing.isPresent()) {
            User user = existing.get();
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
            log.info("Updated existing user: keycloakId={}", keycloakId);
        } else {
            Instant now = Instant.now();
            User user = User.builder()
                    .keycloakId(keycloakId)
                    .username(username)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            userRepository.save(user);
            log.info("Created new user: keycloakId={} username={}", keycloakId, username);
        }
    }
}
