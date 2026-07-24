package org.ultra.rcrs.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
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
        try {
            userAvatarService.saveAvatar(principal.getName(), request.avatar());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (Exception ex) {
            if (ex instanceof jakarta.persistence.EntityNotFoundException) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
            }
            throw ex;
        }
    }
}
