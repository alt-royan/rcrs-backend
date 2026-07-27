package org.ultra.rcrs.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.userservice.dto.UserProfileResponse;
import org.ultra.rcrs.userservice.service.UserAvatarService;
import org.ultra.rcrs.userservice.service.UserService;

import java.security.Principal;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getCompactProfile(@AuthenticationPrincipal Jwt jwt) {
        UserProfileResponse profile = userService.getCompactProfile(jwt.getSubject());
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
        UserProfileResponse profile = userService.getProfile(jwt.getSubject());
        return ResponseEntity.ok(profile);
    }
}
