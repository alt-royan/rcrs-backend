package org.ultra.rcrs.mediaservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.mediaservice.dao.model.Audio;
import org.ultra.rcrs.mediaservice.dao.repository.AudioRepository;
import org.ultra.rcrs.mediaservice.dao.repository.TrackToAudioRepository;
import org.ultra.rcrs.mediaservice.dto.StreamingRequest;
import org.ultra.rcrs.mediaservice.dto.StreamingUrlsDto;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StreamingService {

    private final TrackToAudioRepository trackToAudioRepository;
    private final AudioRepository audioRepository;

    public StreamingUrlsDto stream(StreamingRequest request) {
        var trackId = request.getTrackId();
        UUID mainGuid = trackToAudioRepository.findByTrackIdAndMain(trackId, true)
                .orElseThrow(() -> new NotFoundException("Main audio for track " + trackId + " not found"))
                .getGuid();
        List<Audio> mainAudios = audioRepository.findAllByGuid(mainGuid);
        //TODO: подумать
        return new StreamingUrlsDto();
    }
}
