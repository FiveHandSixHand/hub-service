package com.fhsh.daitda.hubservice.hubinventory.presentation.controller.internal;

import com.fhsh.daitda.hubservice.hubinventory.application.result.DecreaseHubInventoriesByProductResult;
import com.fhsh.daitda.hubservice.hubinventory.application.result.FindHubInventoryResult;
import com.fhsh.daitda.hubservice.hubinventory.application.service.command.HubInventoryCommandService;
import com.fhsh.daitda.hubservice.hubinventory.presentation.dto.request.DecreaseHubInventoriesByProductRequest;
import com.fhsh.daitda.hubservice.hubinventory.presentation.dto.request.DecreaseHubInventoryRequest;
import com.fhsh.daitda.hubservice.hubinventory.presentation.dto.request.RestoreHubInventoryRequest;
import com.fhsh.daitda.hubservice.hubinventory.presentation.dto.response.DecreaseHubInventoriesByProductResponse;
import com.fhsh.daitda.hubservice.hubinventory.presentation.dto.response.DecreaseHubInventoriesResponse;
import com.fhsh.daitda.response.CommonResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/hub-inventories")
public class HubInventoryInternalController {


    private final HubInventoryCommandService hubInventoryCommandService;

    public HubInventoryInternalController(HubInventoryCommandService hubInventoryCommandService) {
        this.hubInventoryCommandService = hubInventoryCommandService;
    }

    // 재고 차감
    @PatchMapping("/decrease")
    public CommonResponse<DecreaseHubInventoriesResponse> decreaseHubInventory(@Valid @RequestBody DecreaseHubInventoryRequest request) {
        List<FindHubInventoryResult> results =
                hubInventoryCommandService.decreaseHubInventories(request.toCommand(), null);

        return CommonResponse.success(DecreaseHubInventoriesResponse.from(results));
    }

    /**
     * 주문 생성용 재고 차감 API
     * supplierCompanyId + productId 기준으로 재고 row 조회
     * 수량 차감
     * 실제 사용한 hubInventoryId 반환
     */
    @PatchMapping("/decrease-by-product")
    public CommonResponse<DecreaseHubInventoriesByProductResponse> decreaseHubInventoriesByProduct(
            @Valid @RequestBody DecreaseHubInventoriesByProductRequest request
    ) {
        DecreaseHubInventoriesByProductResult result =
                hubInventoryCommandService.decreaseHubInventoriesByProduct(request.toCommand(), null);

        return CommonResponse.success(DecreaseHubInventoriesByProductResponse.from(result));
    }

    // 재고 복원
    @PatchMapping("/restoration")
    public CommonResponse<Void> restoreHubInventory(@Valid @RequestBody RestoreHubInventoryRequest request) {
        hubInventoryCommandService.restoreHubInventories(request.toCommand(), null);
        return CommonResponse.success(null);
    }
}
