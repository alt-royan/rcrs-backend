package org.ultra.rcrs.mediaservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.mediaservice.dto.AudioItem;
import org.ultra.rcrs.mediaservice.service.AudioService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/audios")
public class AudioController {

    private final AudioService audioService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<UUID, List<AudioItem>>> getAudiosByTrackId(@RequestParam String trackId) {
        return ResponseEntity.ok(audioService.getAudiosByTrackId(trackId));
    }
}
