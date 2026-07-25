package org.ultra.rcrs.userservice.integration;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.userservice.model.User;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IdentityEventListenerIntegrationTest extends BaseIntegrationTest {

    @Test
    void userCreated_createsNewUser() throws Exception {
        String userId = UUID.randomUUID().toString();
        sendRegisterEvent(userId, "alice", "alice@example.com");
        waitForProcessing();

        assertThat(userRepository.findByUserId(userId)).isPresent();
        User user = userRepository.findByUserId(userId).get();
        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isEmailVerified()).isTrue();
    }

    @Test
    void userCreated_duplicateUserId_updatesExisting() throws Exception {
        String userId = UUID.randomUUID().toString();
        sendRegisterEvent(userId, "bob", "bob@example.com");
        waitForProcessing();

        User first = userRepository.findByUserId(userId).orElseThrow();
        assertThat(first.getUsername()).isEqualTo("bob");

        sendRegisterEvent(userId, "bobby", "bobby@example.com");
        waitForProcessing();

        assertThat(userRepository.count()).isEqualTo(1);
        User updated = userRepository.findByUserId(userId).orElseThrow();
        assertThat(updated.getUsername()).isEqualTo("bobby");
        assertThat(updated.getEmail()).isEqualTo("bobby@example.com");
    }

    @Test
    void userUpdated_updatesProfileFields() throws Exception {
        String userId = UUID.randomUUID().toString();
        sendRegisterEvent(userId, "carol", "carol@example.com");
        waitForProcessing();

        sendUpdateProfileEvent(userId, "carol_new", "carol.new@example.com");
        waitForProcessing();

        User user = userRepository.findByUserId(userId).orElseThrow();
        assertThat(user.getUsername()).isEqualTo("carol_new");
        assertThat(user.getEmail()).isEqualTo("carol.new@example.com");
    }

    @Test
    void userUpdated_unknownUserId_ignored() throws Exception {
        String unknownId = UUID.randomUUID().toString();
        sendUpdateProfileEvent(unknownId, "ghost", "ghost@example.com");
        waitForProcessing();

        assertThat(userRepository.findByUserId(unknownId)).isEmpty();
    }

    @Test
    void userDeleted_softDeletesUser() throws Exception {
        String userId = UUID.randomUUID().toString();
        sendRegisterEvent(userId, "dave", "dave@example.com");
        waitForProcessing();

        User created = userRepository.findByUserId(userId).orElseThrow();
        assertThat(created.isEnabled()).isTrue();

        sendDeleteAccountEvent(userId);
        waitForProcessing();

        User deleted = userRepository.findByUserId(userId).orElseThrow();
        assertThat(deleted.isEnabled()).isFalse();
    }

    @Test
    void userDeleted_unknownUserId_ignored() throws Exception {
        String unknownId = UUID.randomUUID().toString();
        sendDeleteAccountEvent(unknownId);
        waitForProcessing();

        assertThat(userRepository.findByUserId(unknownId)).isEmpty();
        assertThat(userRepository.count()).isZero();
    }

    @Test
    void duplicateEventId_ignored() throws Exception {
        String userId = UUID.randomUUID().toString();
        String eventId = UUID.randomUUID().toString();

        sendKeycloakEvent(eventId, "REGISTER", userId, Map.of(
                "preferred_username", "eve",
                "email", "eve@example.com",
                "email_verified", "true"
        ));
        waitForProcessing();

        assertThat(userRepository.findByUserId(userId)).isPresent();
        assertThat(processedEventRepository.existsByEventId(eventId)).isTrue();

        sendKeycloakEvent(eventId, "REGISTER", userId, Map.of(
                "preferred_username", "eve_changed",
                "email", "eve_changed@example.com",
                "email_verified", "true"
        ));
        waitForProcessing();

        assertThat(userRepository.count()).isEqualTo(1);
        User user = userRepository.findByUserId(userId).orElseThrow();
        assertThat(user.getUsername()).isEqualTo("eve");
    }

    @Test
    void unmappedEventType_ignored() throws Exception {
        String userId = UUID.randomUUID().toString();
        sendKeycloakEvent(UUID.randomUUID().toString(), "LOGOUT", userId, Map.of());
        waitForProcessing();

        assertThat(userRepository.findByUserId(userId)).isEmpty();
    }
}
