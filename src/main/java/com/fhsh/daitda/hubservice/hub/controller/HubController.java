package com.fhsh.daitda.hubservice.hub.controller;

import com.fhsh.daitda.hubservice.hub.dto.request.HubCreateRequest;
import com.fhsh.daitda.hubservice.hub.dto.request.HubUpdateRequest;
import com.fhsh.daitda.hubservice.hub.dto.response.HubResponse;
import com.fhsh.daitda.hubservice.hub.service.HubService;
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
        HubResponse response = hubService.createHub(request, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<HubResponse>> getHubs() {
        List<HubResponse> responses = hubService.getHubs();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{hubId}")
    public ResponseEntity<HubResponse> getHub(@PathVariable UUID hubId) {
        HubResponse response = hubService.getHub(hubId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{hubId}")
    public ResponseEntity<HubResponse> updateHub(@PathVariable UUID hubId,
                                                 @Valid @RequestBody HubUpdateRequest request) {
        HubResponse response = hubService.updateHub(hubId, request, null);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{hubId}")
    public ResponseEntity<Void> deleteHub(@PathVariable UUID hubId) {
        hubService.deleteHub(hubId, null);
        return ResponseEntity.noContent().build();
    }
}
