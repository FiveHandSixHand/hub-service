package com.fhsh.daitda.hubservice.infrastructure.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Kakao Mobility 자동차 길찾기 응답 DTO
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoDirectionsResponse(
        @JsonProperty("trans_id")
        String transId,
        List<Route> routes
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Route(
            @JsonProperty("result_code")
            Integer resultCode,

            @JsonProperty("result_msg")
            String resultMsg,

            Summary summary
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Summary(
            Integer distance,
            Integer duration
    ) {
    }
}