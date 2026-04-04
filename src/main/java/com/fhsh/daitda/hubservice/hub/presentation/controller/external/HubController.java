package com.fhsh.daitda.hubservice.hub.presentation.controller.external;

import com.fhsh.daitda.common.config.security.SecurityHeaderConstants;
import com.fhsh.daitda.common.model.AuthenticatedUser;
import com.fhsh.daitda.hubservice.hub.application.command.CreateHubCommand;
import com.fhsh.daitda.hubservice.hub.application.command.UpdateHubCommand;
import com.fhsh.daitda.hubservice.hub.application.result.FindHubResult;
import com.fhsh.daitda.hubservice.hub.presentation.dto.request.HubCreateRequest;
import com.fhsh.daitda.hubservice.hub.presentation.dto.request.HubUpdateRequest;
import com.fhsh.daitda.hubservice.hub.presentation.dto.response.HubResponse;
import com.fhsh.daitda.hubservice.hub.application.service.HubService;
import com.fhsh.daitda.hubservice.hub.presentation.dto.response.ListHubResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hubs")
public class HubController {

    private final HubService hubService;

    public HubController(HubService hubService) {
        this.hubService = hubService;
    }

    /**
     * 허브를 생성
     * 외부 요청은 Gateway를 통해 진입하므로 사용자 헤더를 받아 createdBy에 반영
     */
    @PostMapping
    public ResponseEntity<HubResponse> createHub(
            @Valid @RequestBody HubCreateRequest request,
            @RequestHeader(value = SecurityHeaderConstants.USER_ID) String userId,
            @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL) String email,
            @RequestHeader(value = SecurityHeaderConstants.USER_ROLE) String role
    ) {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);


        CreateHubCommand command = CreateHubCommand.builder()
                .hubName(request.getHubName())
                .hubAddress(request.getHubAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isCentral(request.getIsCentral())
                .build();

        FindHubResult result = hubService.createHub(command, authenticatedUser.userId());
        HubResponse response = HubResponse.from(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ListHubResponse>> getHubs() {
        List<ListHubResponse> responses = hubService.getHubs()
                .stream()
                .map(hub -> ListHubResponse.from(hub))
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{hubId}")
    public ResponseEntity<HubResponse> getHub(@PathVariable UUID hubId) {
        FindHubResult result = hubService.getHub(hubId);
        HubResponse response = HubResponse.from(result);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{hubId}")
    public ResponseEntity<HubResponse> updateHub(@PathVariable UUID hubId,
                                                 @Valid @RequestBody HubUpdateRequest request,
                                                 @RequestHeader(value = SecurityHeaderConstants.USER_ID) String userId,
                                                 @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL) String email,
                                                 @RequestHeader(value = SecurityHeaderConstants.USER_ROLE)String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);

        UpdateHubCommand command = UpdateHubCommand.builder()
                .hubName(request.getHubName())
                .hubAddress(request.getHubAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isCentral(request.getIsCentral())
                .build();

        FindHubResult result = hubService.updateHub(hubId, command, authenticatedUser.userId());
        HubResponse response = HubResponse.from(result);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{hubId}")
    public ResponseEntity<Void> deleteHub(@PathVariable UUID hubId,
                                          @RequestHeader(value = SecurityHeaderConstants.USER_ID) String userId,
                                          @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL) String email,
                                          @RequestHeader(value = SecurityHeaderConstants.USER_ROLE) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);

        hubService.deleteHub(hubId, authenticatedUser.userId());
        return ResponseEntity.noContent().build();
    }
}
