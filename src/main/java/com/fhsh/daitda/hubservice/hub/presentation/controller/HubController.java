package com.fhsh.daitda.hubservice.hub.presentation.controller;

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

    @PostMapping
    public ResponseEntity<HubResponse> createHub(@Valid @RequestBody HubCreateRequest request) {
        CreateHubCommand command = CreateHubCommand.builder()
                .hubName(request.getHubName())
                .hubAddress(request.getHubAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isCentral(request.getIsCentral())
                .build();

        FindHubResult result = hubService.createHub(command, null);
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
                                                 @Valid @RequestBody HubUpdateRequest request) {
        UpdateHubCommand command = UpdateHubCommand.builder()
                .hubName(request.getHubName())
                .hubAddress(request.getHubAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isCentral(request.getIsCentral())
                .build();

        FindHubResult result = hubService.updateHub(hubId, command, null);
        HubResponse response = HubResponse.from(result);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{hubId}")
    public ResponseEntity<Void> deleteHub(@PathVariable UUID hubId) {
        hubService.deleteHub(hubId, null);
        return ResponseEntity.noContent().build();
    }
}
