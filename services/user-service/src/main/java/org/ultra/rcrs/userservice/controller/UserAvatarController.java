package org.ultra.rcrs.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.userservice.dto.UserAvatarRequest;
import org.ultra.rcrs.userservice.service.UserAvatarService;

import java.security.Principal;

@RestController
@RequestMapping("/avatar")
@RequiredArgsConstructor
public class UserAvatarController {

    private final UserAvatarService userAvatarService;

    @PostMapping
    public ResponseEntity<Void> uploadAvatar(@RequestBody @Valid UserAvatarRequest request,
                                             Principal principal) {
        userAvatarService.saveAvatar(principal.getName(), request.avatar());
        return ResponseEntity.noContent().build();
    }
}
