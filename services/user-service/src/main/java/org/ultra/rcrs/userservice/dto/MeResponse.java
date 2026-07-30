package org.ultra.rcrs.userservice.dto;

import org.ultra.rcrs.enums.ImageSize;

import java.net.URI;
import java.util.List;
import java.util.Map;

public record MeResponse(
        String userId,
        String username,
        List<String> roles,
        Map<ImageSize, URI> avatar) {
}
