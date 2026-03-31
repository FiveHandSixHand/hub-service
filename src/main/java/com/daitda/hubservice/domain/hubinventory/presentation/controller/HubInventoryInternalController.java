package com.daitda.hubservice.domain.hubinventory.presentation.controller;

import com.daitda.hubservice.domain.hubinventory.application.dto.command.DecreaseHubInventoryCommand;
import com.daitda.hubservice.domain.hubinventory.application.dto.command.RestoreHubInventoryCommand;
import com.daitda.hubservice.domain.hubinventory.presentation.dto.request.DecreaseHubInventoryRequest;
import com.daitda.hubservice.domain.hubinventory.presentation.dto.request.RestoreHubInventoryRequest;
import com.daitda.hubservice.domain.hubinventory.presentation.dto.response.FindHubInventoryResponse;
import com.daitda.hubservice.domain.hubinventory.application.service.HubInventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/hub-inventories")
public class HubInventoryInternalController {

    private final HubInventoryService hubInventoryService;

    public HubInventoryInternalController(HubInventoryService hubInventoryService) {
        this.hubInventoryService = hubInventoryService;
    }

    // 재고 차감
    @PatchMapping("/decrease")
    public ResponseEntity<FindHubInventoryResponse> decreaseHubInventory(@Valid @RequestBody DecreaseHubInventoryRequest request) {
        DecreaseHubInventoryCommand command = DecreaseHubInventoryCommand.builder()
                .hubInventoryId(request.getHubInventoryId())
                .quantity(request.getQuantity())
                .build();

        FindHubInventoryResponse response = hubInventoryService.decreaseHubInventory(command, null);

        return ResponseEntity.ok(response);
    }

    // 재고 복원
    @PatchMapping("/restore")
    public ResponseEntity<FindHubInventoryResponse> restoreHubInventory(@Valid @RequestBody RestoreHubInventoryRequest request) {
        RestoreHubInventoryCommand command = RestoreHubInventoryCommand.builder()
                .hubInventoryId(request.getHubInventoryID())
                .quantity(request.getQuantity())
                .build();

        FindHubInventoryResponse response = hubInventoryService.restoreHubInventory(command, null);

        return ResponseEntity.ok(response);
    }
}
