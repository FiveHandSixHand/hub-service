package com.fhsh.daitda.hubservice.infrastructure.naver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

// Naver Directions 5 응답 DTO
@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverDirectionsResponse(
        Integer code,
        String message,
        Map<String, List<Route>> route
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Route(
            Summary summary
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Summary(
            Long distance,
            Integer duration
    ) {
    }

}
