package org.ultra.rcrs.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Resolves the caller's identity from their bearer token.
 * <p>
 * {@code @AuthenticationPrincipal Jwt} is null whenever the request reached the
 * handler without a token — either because the security chain permits the path
 * anonymously, or because security is switched off entirely via
 * {@code spring.security.enabled}. Every endpoint in this service is per-user, so
 * that case is an authentication failure, not a null to dereference: reading the
 * subject directly would surface as a NullPointerException and a 500 instead of
 * the 401 the client needs in order to know it should log in.
 */
public final class CallerId {

    private CallerId() {
    }

    /**
     * @return the token subject — the Keycloak user id every library row is keyed by
     * @throws AuthenticationCredentialsNotFoundException if the request carried no
     *                                                   usable token; the global
     *                                                   exception handler turns this
     *                                                   into a 401
     */
    public static String of(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new AuthenticationCredentialsNotFoundException(
                    "this endpoint requires an authenticated user");
        }
        return jwt.getSubject();
    }

    /**
     * @return the token itself, once confirmed usable, for handlers that need more
     * than the subject
     * @throws AuthenticationCredentialsNotFoundException if the request carried no
     *                                                   usable token; the global
     *                                                   exception handler turns this
     *                                                   into a 401
     */
    public static Jwt require(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new AuthenticationCredentialsNotFoundException(
                    "this endpoint requires an authenticated user");
        }
        return jwt;
    }
}
