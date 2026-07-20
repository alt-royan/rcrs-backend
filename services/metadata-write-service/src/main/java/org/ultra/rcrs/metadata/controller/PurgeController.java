package org.ultra.rcrs.metadata.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.metadata.service.PurgeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class PurgeController {

    private final PurgeService purgeService;

    @PostMapping("/purge")
    public ResponseEntity<Void> purge() {
        purgeService.purge();
        return ResponseEntity.ok().build();
    }
}
