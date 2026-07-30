package org.ultra.rcrs.userservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.userservice.dto.MeResponse;
import org.ultra.rcrs.userservice.service.UserService;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
@Tag(name = "User", description = "Endpoints for retrieving the profile of the currently authenticated user")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<MeResponse> getCompactProfile(@AuthenticationPrincipal Jwt jwt) {
        MeResponse profile = userService.getMe(jwt);
        return ResponseEntity.ok(profile);
    }
}
