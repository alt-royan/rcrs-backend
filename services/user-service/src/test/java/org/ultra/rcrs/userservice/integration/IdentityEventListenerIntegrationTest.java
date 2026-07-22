package org.ultra.rcrs.userservice.integration;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.userservice.model.User;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IdentityEventListenerIntegrationTest extends BaseIntegrationTest {

    @Test
    void userCreated_createsNewUser() throws Exception {
        String keycloakId = UUID.randomUUID().toString();
        sendRegisterEvent(keycloakId, "alice", "alice@example.com", "Alice", "Smith");
        waitForProcessing();

        assertThat(userRepository.findByKeycloakId(keycloakId)).isPresent();
        User user = userRepository.findByKeycloakId(keycloakId).get();
        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getFirstName()).isEqualTo("Alice");
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isEmailVerified()).isTrue();
    }

    @Test
    void userCreated_duplicateKeycloakId_updatesExisting() throws Exception {
        String keycloakId = UUID.randomUUID().toString();
        sendRegisterEvent(keycloakId, "bob", "bob@example.com", "Bob", "Jones");
        waitForProcessing();

        User first = userRepository.findByKeycloakId(keycloakId).orElseThrow();
        assertThat(first.getUsername()).isEqualTo("bob");

        sendRegisterEvent(keycloakId, "bobby", "bobby@example.com", "Bobby", "Jones");
        waitForProcessing();

        assertThat(userRepository.count()).isEqualTo(1);
        User updated = userRepository.findByKeycloakId(keycloakId).orElseThrow();
        assertThat(updated.getUsername()).isEqualTo("bobby");
        assertThat(updated.getEmail()).isEqualTo("bobby@example.com");
        assertThat(updated.getFirstName()).isEqualTo("Bobby");
    }

    @Test
    void userUpdated_updatesProfileFields() throws Exception {
        String keycloakId = UUID.randomUUID().toString();
        sendRegisterEvent(keycloakId, "carol", "carol@example.com", "Carol", "White");
        waitForProcessing();

        sendUpdateProfileEvent(keycloakId, "carol_new", "carol.new@example.com", "Carolyn", "White");
        waitForProcessing();

        User user = userRepository.findByKeycloakId(keycloakId).orElseThrow();
        assertThat(user.getUsername()).isEqualTo("carol_new");
        assertThat(user.getEmail()).isEqualTo("carol.new@example.com");
        assertThat(user.getFirstName()).isEqualTo("Carolyn");
    }

    @Test
    void userUpdated_unknownKeycloakId_ignored() throws Exception {
        String unknownId = UUID.randomUUID().toString();
        sendUpdateProfileEvent(unknownId, "ghost", "ghost@example.com", "Ghost", "User");
        waitForProcessing();

        assertThat(userRepository.findByKeycloakId(unknownId)).isEmpty();
    }

    @Test
    void userDeleted_softDeletesUser() throws Exception {
        String keycloakId = UUID.randomUUID().toString();
        sendRegisterEvent(keycloakId, "dave", "dave@example.com", "Dave", "Brown");
        waitForProcessing();

        User created = userRepository.findByKeycloakId(keycloakId).orElseThrow();
        assertThat(created.isEnabled()).isTrue();

        sendDeleteAccountEvent(keycloakId);
        waitForProcessing();

        User deleted = userRepository.findByKeycloakId(keycloakId).orElseThrow();
        assertThat(deleted.isEnabled()).isFalse();
    }

    @Test
    void userDeleted_unknownKeycloakId_ignored() throws Exception {
        String unknownId = UUID.randomUUID().toString();
        sendDeleteAccountEvent(unknownId);
        waitForProcessing();

        assertThat(userRepository.findByKeycloakId(unknownId)).isEmpty();
        assertThat(userRepository.count()).isZero();
    }

    @Test
    void duplicateEventId_ignored() throws Exception {
        String keycloakId = UUID.randomUUID().toString();
        String eventId = UUID.randomUUID().toString();

        sendKeycloakEvent(eventId, "REGISTER", keycloakId, Map.of(
                "preferred_username", "eve",
                "email", "eve@example.com",
                "first_name", "Eve",
                "last_name", "Davis",
                "email_verified", "true"
        ));
        waitForProcessing();

        assertThat(userRepository.findByKeycloakId(keycloakId)).isPresent();
        assertThat(processedEventRepository.existsByEventId(eventId)).isTrue();

        sendKeycloakEvent(eventId, "REGISTER", keycloakId, Map.of(
                "preferred_username", "eve_changed",
                "email", "eve_changed@example.com",
                "first_name", "Eve",
                "last_name", "Davis",
                "email_verified", "true"
        ));
        waitForProcessing();

        assertThat(userRepository.count()).isEqualTo(1);
        User user = userRepository.findByKeycloakId(keycloakId).orElseThrow();
        assertThat(user.getUsername()).isEqualTo("eve");
    }

    @Test
    void unmappedEventType_ignored() throws Exception {
        String keycloakId = UUID.randomUUID().toString();
        sendKeycloakEvent(UUID.randomUUID().toString(), "LOGOUT", keycloakId, Map.of());
        waitForProcessing();

        assertThat(userRepository.findByKeycloakId(keycloakId)).isEmpty();
    }
}
