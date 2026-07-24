package org.ultra.rcrs.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.userservice.dto.UserProfileResponse;
import org.ultra.rcrs.userservice.service.UserAvatarService;

import java.security.Principal;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserAvatarService userAvatarService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(
            @RequestParam(name = "full", required = false, defaultValue = "false") boolean full,
            Principal principal) {
        UserProfileResponse profile = userAvatarService.getProfile(principal.getName(), full);
        return ResponseEntity.ok(profile);
    }
}
