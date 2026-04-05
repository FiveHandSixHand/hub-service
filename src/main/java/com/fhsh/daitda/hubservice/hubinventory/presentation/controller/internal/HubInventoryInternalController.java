package com.fhsh.daitda.hubservice.hubinventory.presentation.controller.internal;

import com.fhsh.daitda.hubservice.hubinventory.application.command.DecreaseHubInventoryCommand;
import com.fhsh.daitda.hubservice.hubinventory.application.command.RestoreHubInventoryCommand;
import com.fhsh.daitda.hubservice.hubinventory.application.result.FindHubInventoryResult;
import com.fhsh.daitda.hubservice.hubinventory.application.service.command.HubInventoryCommandService;
import com.fhsh.daitda.hubservice.hubinventory.presentation.dto.request.DecreaseHubInventoryRequest;
import com.fhsh.daitda.hubservice.hubinventory.presentation.dto.request.RestoreHubInventoryRequest;
import com.fhsh.daitda.hubservice.hubinventory.presentation.dto.response.FindHubInventoryResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/hub-inventories")
public class HubInventoryInternalController {

    private final HubInventoryCommandService hubInventoryCommandService;

    public HubInventoryInternalController(HubInventoryCommandService hubInventoryCommandService) {
        this.hubInventoryCommandService = hubInventoryCommandService;
    }

    // 재고 차감
    @PatchMapping("/decrease")
    public ResponseEntity<FindHubInventoryResponse> decreaseHubInventory(@Valid @RequestBody DecreaseHubInventoryRequest request) {
        DecreaseHubInventoryCommand command = DecreaseHubInventoryCommand.builder()
                .hubInventoryId(request.getHubInventoryId())
                .quantity(request.getQuantity())
                .build();

        FindHubInventoryResult result = hubInventoryCommandService.decreaseHubInventory(command, null);

        return ResponseEntity.ok(FindHubInventoryResponse.from(result));
    }

    // 재고 복원
    @PatchMapping("/restore")
    public ResponseEntity<FindHubInventoryResponse> restoreHubInventory(@Valid @RequestBody RestoreHubInventoryRequest request) {
        RestoreHubInventoryCommand command = RestoreHubInventoryCommand.builder()
                .hubInventoryId(request.getHubInventoryId())
                .quantity(request.getQuantity())
                .build();

        FindHubInventoryResult result = hubInventoryCommandService.restoreHubInventory(command, null);

        return ResponseEntity.ok(FindHubInventoryResponse.from(result));
    }
}
