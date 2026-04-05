package com.fhsh.daitda.hubservice.hubinventory.application.service;

import com.fhsh.daitda.exception.BusinessException;
import com.fhsh.daitda.hubservice.hubinventory.application.command.CreateHubInventoryCommand;
import com.fhsh.daitda.hubservice.hubinventory.application.command.DecreaseHubInventoriesCommand;
import com.fhsh.daitda.hubservice.hubinventory.application.command.DecreaseHubInventoryCommand;
import com.fhsh.daitda.hubservice.hubinventory.application.result.FindHubInventoryResult;
import com.fhsh.daitda.hubservice.hubinventory.application.service.command.HubInventoryCommandService;
import com.fhsh.daitda.hubservice.hubinventory.domain.entity.HubInventory;
import com.fhsh.daitda.hubservice.hubinventory.domain.exception.HubInventoryErrorCode;
import com.fhsh.daitda.hubservice.hubinventory.domain.repository.HubInventoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HubInventoryServiceTest {

    @Mock
    private HubInventoryRepository hubInventoryRepository;

    @InjectMocks
    private HubInventoryCommandService hubInventoryCommandService;

    private static final UUID HUB_ID = UUID.randomUUID();
    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID HUB_INVENTORY_ID = UUID.randomUUID();
    private static final String USER_ID = "test-user";

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

        when(hubInventoryRepository.saveAndFlush(any(HubInventory.class)))
                .thenAnswer(invocationOnMock -> {
                    HubInventory inventory = invocationOnMock.getArgument(0);
                    ReflectionTestUtils.invokeMethod(inventory, "prePersist");
                    ReflectionTestUtils.setField(inventory, "hubInventoryId", HUB_INVENTORY_ID);
                    ReflectionTestUtils.setField(inventory,"createdAt", LocalDateTime.now());
                    return inventory;
                });

        // when
        FindHubInventoryResult result = hubInventoryCommandService.createHubInventory(command, USER_ID);

        // then
        assertThat(result.hubInventoryId()).isEqualTo(HUB_INVENTORY_ID);
        assertThat(result.hubId()).isEqualTo(HUB_ID);
        assertThat(result.companyId()).isEqualTo(COMPANY_ID);
        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.quantity()).isEqualTo(100);
        assertThat(result.createdAt()).isNotNull();

        verify(hubInventoryRepository).existsByHubIdAndCompanyIdAndProductIdAndDeletedAtIsNull(HUB_ID, COMPANY_ID, PRODUCT_ID);
        verify(hubInventoryRepository).saveAndFlush(any(HubInventory.class));
    }

    @Test
    @DisplayName("중복된 허브 재고는 생성할 수 없다")
    void 중복재고생성실패() {
        // given
        CreateHubInventoryCommand command = CreateHubInventoryCommand.builder()
                .hubId(HUB_ID)
                .companyId(COMPANY_ID)
                .productId(PRODUCT_ID)
                .quantity(100)
                .build();

        when(hubInventoryRepository.existsByHubIdAndCompanyIdAndProductIdAndDeletedAtIsNull(HUB_ID, COMPANY_ID, PRODUCT_ID))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> hubInventoryCommandService.createHubInventory(command, USER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(HubInventoryErrorCode.HUB_INVENTORY_CONFLICT);

        verify(hubInventoryRepository, never()).saveAndFlush(any(HubInventory.class));
    }

    @Test
    @DisplayName("재고보다 많은 수량은 차감할 수 없다")
    void 재고부족차감실패() {
        // given
        HubInventory hubInventory = 생성된재고(2);

        DecreaseHubInventoryCommand command = DecreaseHubInventoryCommand.builder()
                .hubInventoryId(HUB_INVENTORY_ID)
                .quantity(3)
                .build();

        when(hubInventoryRepository.findByHubInventoryIdAndDeletedAtIsNull(HUB_INVENTORY_ID))
                .thenReturn(Optional.of(hubInventory));

        // when & then
        assertThatThrownBy(() -> hubInventoryCommandService.decreaseHubInventory(command, USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("재고가 부족합니다.");
    }

    @Test
    @DisplayName("DB 유니크 제약 위반 시 중복 예외 반환")
    void 중복재고생성_DB제약위반실패(){
        // given
        CreateHubInventoryCommand command = CreateHubInventoryCommand.builder()
                .hubId(HUB_ID)
                .companyId(COMPANY_ID)
                .productId(PRODUCT_ID)
                .quantity(100)
                .build();

        when(hubInventoryRepository.existsByHubIdAndCompanyIdAndProductIdAndDeletedAtIsNull(HUB_ID, COMPANY_ID, PRODUCT_ID))
                .thenReturn(false);

        when(hubInventoryRepository.saveAndFlush(any(HubInventory.class)))
                .thenThrow(new DataIntegrityViolationException("고유 제약 조건 위반"));

        // when & then
        assertThatThrownBy(() -> hubInventoryCommandService.createHubInventory(command, USER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(HubInventoryErrorCode.HUB_INVENTORY_CONFLICT);
    }

    @Test
    @DisplayName("여러 재고를 한 번에 차감")
    void 여러재고_일괄차감성공(){
        // given
        UUID HUB_INVENTORY_ID_1 = UUID.randomUUID();
        UUID HUB_INVENTORY_ID_2 = UUID.randomUUID();

        HubInventory inventory1 = HubInventory.create(HUB_ID, COMPANY_ID, PRODUCT_ID, 100, USER_ID);
        ReflectionTestUtils.invokeMethod(inventory1, "prePersist");
        ReflectionTestUtils.setField(inventory1, "hubInventoryId", HUB_INVENTORY_ID_1);

        HubInventory inventory2 = HubInventory.create(HUB_ID, COMPANY_ID, UUID.randomUUID(), 50, USER_ID);
        ReflectionTestUtils.invokeMethod(inventory2, "prePersist");
        ReflectionTestUtils.setField(inventory2, "hubInventoryId", HUB_INVENTORY_ID_2);

        DecreaseHubInventoriesCommand command = DecreaseHubInventoriesCommand.builder()
                .items(List.of(
                        DecreaseHubInventoriesCommand.Item.builder()
                                .hubInventoryId(HUB_INVENTORY_ID_1)
                                .quantity(10)
                                .build(),
                        DecreaseHubInventoriesCommand.Item.builder()
                                .hubInventoryId(HUB_INVENTORY_ID_2)
                                .quantity(5)
                                .build()
                ))
                .build();

        when(hubInventoryRepository.findByHubInventoryIdAndDeletedAtIsNull(HUB_INVENTORY_ID_1))
                .thenReturn(Optional.of(inventory1));
        when(hubInventoryRepository.findByHubInventoryIdAndDeletedAtIsNull(HUB_INVENTORY_ID_2))
                .thenReturn(Optional.of(inventory2));

        // when
        List<FindHubInventoryResult> results = hubInventoryCommandService.decreaseHubInventories(command, USER_ID);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).quantity()).isEqualTo(90);
        assertThat(results.get(1).quantity()).isEqualTo(45);
    }

    private HubInventory 생성된재고(int quantity) {
        HubInventory hubInventory = HubInventory.create(HUB_ID, COMPANY_ID, PRODUCT_ID, quantity, USER_ID);

        ReflectionTestUtils.invokeMethod(hubInventory, "prePersist");
        ReflectionTestUtils.setField(hubInventory, "hubInventoryId", HUB_INVENTORY_ID);

        return hubInventory;
    }
}
