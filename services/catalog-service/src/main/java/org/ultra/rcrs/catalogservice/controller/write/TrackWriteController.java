
package org.ultra.rcrs.catalogservice.controller.write;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.catalogservice.service.write.TrackWriteService;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/metadata/tracks")
@ConditionalOnProperty(name = "app.write.enabled", havingValue = "true")
public class TrackWriteController {

    private final TrackWriteService trackWriteService;

    @DeleteMapping("/{trackId}")
    public Mono<ResponseEntity<Void>> deleteTrack(@PathVariable("trackId") String trackId) {
        return trackWriteService.deleteTrack(Url62.decode(trackId))
                .map(ResponseEntity::ok);
    }

}

