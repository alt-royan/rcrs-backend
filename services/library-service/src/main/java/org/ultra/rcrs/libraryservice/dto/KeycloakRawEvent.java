package org.ultra.rcrs.libraryservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The subset of Keycloak's raw admin-event JSON that this service needs.
 * <p>
 * Only the deletion case matters here, so unlike user-service — which mirrors the
 * full event to keep profiles in sync — this deliberately binds just the three
 * fields required to identify a deleted account, and ignores everything else.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakRawEvent {

    private String id;

    private String type;

    private String userId;
}
