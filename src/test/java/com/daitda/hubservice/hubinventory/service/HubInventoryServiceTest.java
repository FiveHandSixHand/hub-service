package com.daitda.hubservice.hubinventory.service;

import com.daitda.hubservice.domain.hubinventory.application.dto.CreateHubInventoryCommand;
import com.daitda.hubservice.domain.hubinventory.application.service.HubInventoryService;
import com.daitda.hubservice.domain.hubinventory.presentation.dto.response.FindHubInventoryResponse;
import com.daitda.hubservice.domain.hubinventory.domain.entity.HubInventory;
import com.daitda.hubservice.domain.hubinventory.domain.repository.HubInventoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HubInventoryServiceTest {

    @Mock
    private HubInventoryRepository hubInventoryRepository;

    @InjectMocks
    private HubInventoryService hubInventoryService;

    private static final UUID HUB_ID = UUID.randomUUID();
    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID HUB_INVENTORY_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    @DisplayName("허브 재고 생성 성공")
    void 재고생성() {
        // given
        CreateHubInventoryCommand command = CreateHubInventoryCommand.builder()
                .hubId(HUB_ID)
                .companyId(COMPANY_ID)
                .productId(PRODUCT_ID)
                .quantity(100)
                .build();

        when(hubInventoryRepository.existsByHubIdAndCompanyIdAndProductIdAndDeletedAtIsNull(HUB_ID, COMPANY_ID, PRODUCT_ID))
                .thenReturn(false);

        when(hubInventoryRepository.save(any(HubInventory.class)))
                .thenAnswer(invocationOnMock -> {
                    HubInventory inventory = invocationOnMock.getArgument(0);
                    ReflectionTestUtils.invokeMethod(inventory, "prePersist");
                    ReflectionTestUtils.setField(inventory, "hubInventoryId", HUB_INVENTORY_ID);
                    return inventory;
                });

        // when
        FindHubInventoryResponse response = hubInventoryService.createHubInventory(command, USER_ID);

        // then
        assertThat(response.getHubInventoryId()).isEqualTo(HUB_INVENTORY_ID);
        assertThat(response.getHubId()).isEqualTo(HUB_ID);
        assertThat(response.getCompanyId()).isEqualTo(COMPANY_ID);
        assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(response.getQuantity()).isEqualTo(100);
        assertThat(response.getCreatedAt()).isNotNull();

        verify(hubInventoryRepository).existsByHubIdAndCompanyIdAndProductIdAndDeletedAtIsNull(HUB_ID, COMPANY_ID, PRODUCT_ID);
        verify(hubInventoryRepository).save(any(HubInventory.class));
    }
}
