package com.fhsh.daitda.hubservice.hubroute.application.service.query;

import com.fhsh.daitda.exception.BusinessException;
import com.fhsh.daitda.hubservice.hub.domain.entity.Hub;
import com.fhsh.daitda.hubservice.hub.domain.repository.HubRepository;
import com.fhsh.daitda.hubservice.hubroute.application.result.FindHubRoutePathResult;
import com.fhsh.daitda.hubservice.hubroute.application.result.FindHubRouteResult;
import com.fhsh.daitda.hubservice.hubroute.domain.entity.HubRoute;
import com.fhsh.daitda.hubservice.hubroute.domain.exception.HubRouteErrorCode;
import com.fhsh.daitda.hubservice.hubroute.domain.repository.HubRouteRepository;
import com.fhsh.daitda.hubservice.infrastructure.kakao.client.KakaoDirectionsClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HubRouteQueryServiceTest {

    @Mock
    private HubRouteRepository hubRouteRepository;

    @Mock
    private HubRepository hubRepository;

    @Mock
    private KakaoDirectionsClient kakaoDirectionsClient;

    @InjectMocks
    private HubRouteQueryService hubRouteQueryService;

    private static final UUID USER_ID = UUID.randomUUID();

    private static final UUID SRC_HUB_ID = UUID.randomUUID();
    private static final UUID DEST_HUB_ID = UUID.randomUUID();
    private static final UUID RELAY_HUB_ID = UUID.randomUUID();
    private static final UUID ANOTHER_HUB_ID = UUID.randomUUID();

    @Test
    @DisplayName("허브 간 경로 조회 시 허브명, 주소, 표시용 시간/거리를 포함한다")
    void 허브간경로조회_허브상세포함() {
        // given
        HubRoute directRoute = 생성된경로(UUID.randomUUID(), SRC_HUB_ID, DEST_HUB_ID, 81, "86.19");

        Hub srcHub = 생성된허브(
                SRC_HUB_ID,
                "경기 남부 센터",
                "경기도 이천시 덕평로 257-21",
                "127.4230",
                "37.1896"
        );

        Hub destHub = 생성된허브(
                DEST_HUB_ID,
                "경기 북부 센터",
                "경기도 고양시 덕양구 권율대로 570",
                "126.8730",
                "37.6400"
        );

        when(hubRouteRepository.findBySrcHubIdAndDestHubIdAndDeletedAtIsNull(SRC_HUB_ID, DEST_HUB_ID))
                .thenReturn(Optional.of(directRoute));
        when(hubRepository.findByHubIdAndDeletedAtIsNull(SRC_HUB_ID))
                .thenReturn(Optional.of(srcHub));
        when(hubRepository.findByHubIdAndDeletedAtIsNull(DEST_HUB_ID))
                .thenReturn(Optional.of(destHub));

        // when
        FindHubRouteResult result = hubRouteQueryService.searchHubRoute(SRC_HUB_ID, DEST_HUB_ID);

        // then
        assertThat(result.hubRouteId()).isEqualTo(directRoute.getHubRouteId());
        assertThat(result.srcHubName()).isEqualTo("경기 남부 센터");
        assertThat(result.srcHubAddress()).isEqualTo("경기도 이천시 덕평로 257-21");
        assertThat(result.destHubName()).isEqualTo("경기 북부 센터");
        assertThat(result.destHubAddress()).isEqualTo("경기도 고양시 덕양구 권율대로 570");
        assertThat(result.durationTime()).isEqualTo(81);
        assertThat(result.durationText()).isEqualTo("1시간 21분");
        assertThat(result.distance()).isEqualByComparingTo("86.19");
        assertThat(result.distanceText()).isEqualTo("86.19km");
    }

    @Test
    @DisplayName("직행 거리가 200km 미만이면 직행 1건을 반환한다")
    void 최종배송경로조회_직행_200km미만() {
        // given
        Hub srcHub = 생성된허브(
                SRC_HUB_ID,
                "경기 남부 센터",
                "경기도 이천시 덕평로 257-21",
                "127.4230",
                "37.1896"
        );

        Hub destHub = 생성된허브(
                DEST_HUB_ID,
                "경기 북부 센터",
                "경기도 고양시 덕양구 권율대로 570",
                "126.8730",
                "37.6400"
        );

        HubRoute directStoredRoute = 생성된경로(UUID.randomUUID(), SRC_HUB_ID, DEST_HUB_ID, 81, "86.19");

        when(hubRepository.findByHubIdAndDeletedAtIsNull(SRC_HUB_ID))
                .thenReturn(Optional.of(srcHub));
        when(hubRepository.findByHubIdAndDeletedAtIsNull(DEST_HUB_ID))
                .thenReturn(Optional.of(destHub));

        when(kakaoDirectionsClient.getDrivingMetrics(
                srcHub.getLongitude(), srcHub.getLatitude(),
                destHub.getLongitude(), destHub.getLatitude()
        )).thenReturn(new KakaoDirectionsClient.RouteMetrics(
                81,
                86_190L,
                new BigDecimal("86.19")
        ));

        when(hubRouteRepository.findBySrcHubIdAndDestHubIdAndDeletedAtIsNull(SRC_HUB_ID, DEST_HUB_ID))
                .thenReturn(Optional.of(directStoredRoute));

        // when
        List<FindHubRoutePathResult> results = hubRouteQueryService.getHubRoutePath(SRC_HUB_ID, DEST_HUB_ID);

        // then
        assertThat(results).hasSize(1);

        FindHubRoutePathResult result = results.get(0);
        assertThat(result.sequence()).isEqualTo(1);
        assertThat(result.hubRouteId()).isEqualTo(directStoredRoute.getHubRouteId());
        assertThat(result.srcHubId()).isEqualTo(SRC_HUB_ID);
        assertThat(result.destHubId()).isEqualTo(DEST_HUB_ID);
        assertThat(result.srcHubName()).isEqualTo("경기 남부 센터");
        assertThat(result.destHubName()).isEqualTo("경기 북부 센터");
        assertThat(result.durationTime()).isEqualTo(81);
        assertThat(result.durationText()).isEqualTo("1시간 21분");
        assertThat(result.distance()).isEqualByComparingTo("86.19");
        assertThat(result.distanceText()).isEqualTo("86.19km");
    }

    @Test
    @DisplayName("직행 거리가 200km 이상이면 relay hub를 선정해 2구간으로 분할한다")
    void 최종배송경로조회_직행_200km이상_정책분할() {
        // given
        Hub srcHub = 생성된허브(
                SRC_HUB_ID,
                "대구광역시 센터",
                "대구 북구 태평로 161",
                "128.5910",
                "35.8714"
        );

        Hub destHub = 생성된허브(
                DEST_HUB_ID,
                "인천광역시 센터",
                "인천 남동구 정각로 29",
                "126.7052",
                "37.4563"
        );

        Hub relayHub = 생성된허브(
                RELAY_HUB_ID,
                "대전광역시 센터",
                "대전 서구 둔산로 100",
                "127.3845",
                "36.3504"
        );

        Hub anotherHub = 생성된허브(
                ANOTHER_HUB_ID,
                "광주광역시 센터",
                "광주 서구 내방로 111",
                "126.8514",
                "35.1595"
        );

        HubRoute firstStoredRoute = 생성된경로(UUID.randomUUID(), SRC_HUB_ID, RELAY_HUB_ID, 125, "151.76");
        HubRoute secondStoredRoute = 생성된경로(UUID.randomUUID(), RELAY_HUB_ID, DEST_HUB_ID, 153, "173.65");

        when(hubRepository.findByHubIdAndDeletedAtIsNull(SRC_HUB_ID))
                .thenReturn(Optional.of(srcHub));
        when(hubRepository.findByHubIdAndDeletedAtIsNull(DEST_HUB_ID))
                .thenReturn(Optional.of(destHub));
        when(hubRepository.findAllByDeletedAtIsNull())
                .thenReturn(List.of(srcHub, destHub, relayHub, anotherHub));

        // 직행은 200km 이상
        when(kakaoDirectionsClient.getDrivingMetrics(
                srcHub.getLongitude(), srcHub.getLatitude(),
                destHub.getLongitude(), destHub.getLatitude()
        )).thenReturn(new KakaoDirectionsClient.RouteMetrics(
                227,
                299_740L,
                new BigDecimal("299.74")
        ));

        // relayHub 후보: 두 구간 모두 200km 미만
        when(kakaoDirectionsClient.getDrivingMetrics(
                srcHub.getLongitude(), srcHub.getLatitude(),
                relayHub.getLongitude(), relayHub.getLatitude()
        )).thenReturn(new KakaoDirectionsClient.RouteMetrics(
                125,
                151_760L,
                new BigDecimal("151.76")
        ));
        when(kakaoDirectionsClient.getDrivingMetrics(
                relayHub.getLongitude(), relayHub.getLatitude(),
                destHub.getLongitude(), destHub.getLatitude()
        )).thenReturn(new KakaoDirectionsClient.RouteMetrics(
                153,
                173_650L,
                new BigDecimal("173.65")
        ));

        // anotherHub 후보: 유효하지만 총 거리가 더 김
        when(kakaoDirectionsClient.getDrivingMetrics(
                srcHub.getLongitude(), srcHub.getLatitude(),
                anotherHub.getLongitude(), anotherHub.getLatitude()
        )).thenReturn(new KakaoDirectionsClient.RouteMetrics(
                150,
                180_000L,
                new BigDecimal("180.00")
        ));
        when(kakaoDirectionsClient.getDrivingMetrics(
                anotherHub.getLongitude(), anotherHub.getLatitude(),
                destHub.getLongitude(), destHub.getLatitude()
        )).thenReturn(new KakaoDirectionsClient.RouteMetrics(
                165,
                190_000L,
                new BigDecimal("190.00")
        ));

        when(hubRouteRepository.findBySrcHubIdAndDestHubIdAndDeletedAtIsNull(SRC_HUB_ID, RELAY_HUB_ID))
                .thenReturn(Optional.of(firstStoredRoute));
        when(hubRouteRepository.findBySrcHubIdAndDestHubIdAndDeletedAtIsNull(RELAY_HUB_ID, DEST_HUB_ID))
                .thenReturn(Optional.of(secondStoredRoute));

        // when
        List<FindHubRoutePathResult> results = hubRouteQueryService.getHubRoutePath(SRC_HUB_ID, DEST_HUB_ID);

        // then
        assertThat(results).hasSize(2);

        FindHubRoutePathResult first = results.get(0);
        assertThat(first.sequence()).isEqualTo(1);
        assertThat(first.hubRouteId()).isEqualTo(firstStoredRoute.getHubRouteId());
        assertThat(first.srcHubId()).isEqualTo(SRC_HUB_ID);
        assertThat(first.destHubId()).isEqualTo(RELAY_HUB_ID);
        assertThat(first.srcHubName()).isEqualTo("대구광역시 센터");
        assertThat(first.destHubName()).isEqualTo("대전광역시 센터");
        assertThat(first.durationText()).isEqualTo("2시간 5분");
        assertThat(first.distanceText()).isEqualTo("151.76km");

        FindHubRoutePathResult second = results.get(1);
        assertThat(second.sequence()).isEqualTo(2);
        assertThat(second.hubRouteId()).isEqualTo(secondStoredRoute.getHubRouteId());
        assertThat(second.srcHubId()).isEqualTo(RELAY_HUB_ID);
        assertThat(second.destHubId()).isEqualTo(DEST_HUB_ID);
        assertThat(second.srcHubName()).isEqualTo("대전광역시 센터");
        assertThat(second.destHubName()).isEqualTo("인천광역시 센터");
        assertThat(second.durationText()).isEqualTo("2시간 33분");
        assertThat(second.distanceText()).isEqualTo("173.65km");
    }

    @Test
    @DisplayName("직행 거리가 200km 이상이어도 유효한 relay hub가 없으면 예외가 발생한다")
    void 최종배송경로조회_relay후보없음_예외() {
        // given
        Hub srcHub = 생성된허브(
                SRC_HUB_ID,
                "대구광역시 센터",
                "대구 북구 태평로 161",
                "128.5910",
                "35.8714"
        );

        Hub destHub = 생성된허브(
                DEST_HUB_ID,
                "인천광역시 센터",
                "인천 남동구 정각로 29",
                "126.7052",
                "37.4563"
        );

        Hub relayHub = 생성된허브(
                RELAY_HUB_ID,
                "광주광역시 센터",
                "광주 서구 내방로 111",
                "126.8514",
                "35.1595"
        );

        when(hubRepository.findByHubIdAndDeletedAtIsNull(SRC_HUB_ID))
                .thenReturn(Optional.of(srcHub));
        when(hubRepository.findByHubIdAndDeletedAtIsNull(DEST_HUB_ID))
                .thenReturn(Optional.of(destHub));
        when(hubRepository.findAllByDeletedAtIsNull())
                .thenReturn(List.of(srcHub, destHub, relayHub));

        // 직행은 200km 이상
        when(kakaoDirectionsClient.getDrivingMetrics(
                srcHub.getLongitude(), srcHub.getLatitude(),
                destHub.getLongitude(), destHub.getLatitude()
        )).thenReturn(new KakaoDirectionsClient.RouteMetrics(
                227,
                299_740L,
                new BigDecimal("299.74")
        ));

        // relay 후보가 한 구간이라도 200km 이상이라 탈락
        when(kakaoDirectionsClient.getDrivingMetrics(
                srcHub.getLongitude(), srcHub.getLatitude(),
                relayHub.getLongitude(), relayHub.getLatitude()
        )).thenReturn(new KakaoDirectionsClient.RouteMetrics(
                170,
                210_000L,
                new BigDecimal("210.00")
        ));
        when(kakaoDirectionsClient.getDrivingMetrics(
                relayHub.getLongitude(), relayHub.getLatitude(),
                destHub.getLongitude(), destHub.getLatitude()
        )).thenReturn(new KakaoDirectionsClient.RouteMetrics(
                120,
                150_000L,
                new BigDecimal("150.00")
        ));

        // when & then
        assertThatThrownBy(() -> hubRouteQueryService.getHubRoutePath(SRC_HUB_ID, DEST_HUB_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(HubRouteErrorCode.HUB_ROUTE_NOT_FOUND);
    }

    @Test
    @DisplayName("출발 허브와 도착 허브가 같으면 예외가 발생한다")
    void 최종배송경로조회_같은허브_예외() {
        assertThatThrownBy(() -> hubRouteQueryService.getHubRoutePath(SRC_HUB_ID, SRC_HUB_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("출발 허브와 도착 허브는 같을 수 없습니다.");
    }

    private Hub 생성된허브(
            UUID hubId,
            String hubName,
            String hubAddress,
            String longitude,
            String latitude
    ) {
        Hub hub = Hub.create(
                hubName,
                hubAddress,
                new BigDecimal(longitude),
                new BigDecimal(latitude),
                false,
                USER_ID
        );

        ReflectionTestUtils.invokeMethod(hub, "prePersist");
        ReflectionTestUtils.setField(hub, "hubId", hubId);
        return hub;
    }

    private HubRoute 생성된경로(
            UUID hubRouteId,
            UUID srcHubId,
            UUID destHubId,
            Integer durationTime,
            String distance
    ) {
        HubRoute hubRoute = HubRoute.create(
                srcHubId,
                destHubId,
                durationTime,
                new BigDecimal(distance),
                USER_ID
        );

        ReflectionTestUtils.invokeMethod(hubRoute, "prePersist");
        ReflectionTestUtils.setField(hubRoute, "hubRouteId", hubRouteId);
        ReflectionTestUtils.setField(hubRoute, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(hubRoute, "updatedAt", LocalDateTime.now());
        return hubRoute;
    }
}