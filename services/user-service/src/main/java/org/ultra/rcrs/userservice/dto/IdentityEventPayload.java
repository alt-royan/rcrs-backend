package org.ultra.rcrs.userservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IdentityEventPayload {

    private String userId;
    private String username;
    private String email;
    private boolean enabled;
    private boolean emailVerified;
}
