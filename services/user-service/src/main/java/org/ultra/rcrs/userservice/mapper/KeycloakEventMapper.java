package org.ultra.rcrs.userservice.mapper;

import org.springframework.stereotype.Component;
import org.ultra.rcrs.userservice.dto.IdentityEvent;
import org.ultra.rcrs.userservice.dto.IdentityEventPayload;
import org.ultra.rcrs.userservice.dto.IdentityEventType;
import org.ultra.rcrs.userservice.dto.KeycloakRawEvent;

import java.time.Instant;
import java.util.Map;

@Component
public class KeycloakEventMapper {

    private static final Map<String, IdentityEventType> EVENT_TYPE_MAP = Map.of(
            "REGISTER", IdentityEventType.USER_CREATED,
            "UPDATE_PROFILE", IdentityEventType.USER_UPDATED,
            "UPDATE_EMAIL", IdentityEventType.USER_UPDATED,
            "DELETE_ACCOUNT", IdentityEventType.USER_DELETED,
            "LOGIN", IdentityEventType.USER_UPDATED
    );

    public IdentityEvent toIdentityEvent(KeycloakRawEvent raw) {
        IdentityEventType eventType = EVENT_TYPE_MAP.get(raw.getType());
        if (eventType == null) {
            return null;
        }

        Map<String, String> details = raw.getDetails() != null ? raw.getDetails() : Map.of();

        IdentityEventPayload payload = IdentityEventPayload.builder()
                .keycloakId(raw.getUserId())
                .username(details.getOrDefault("preferred_username", details.get("username")))
                .email(details.get("email"))
                .firstName(details.get("first_name"))
                .lastName(details.get("last_name"))
                .enabled(!"DELETE_ACCOUNT".equals(raw.getType()))
                .emailVerified("true".equals(details.get("email_verified")))
                .build();

        return IdentityEvent.builder()
                .eventId(raw.getId())
                .eventType(eventType)
                .schemaVersion(1)
                .source("keycloak")
                .occurredAt(raw.getTime() > 0 ? Instant.ofEpochMilli(raw.getTime()) : Instant.now())
                .payload(payload)
                .build();
    }
}
