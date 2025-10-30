package com.capstone.web.refrigerator.service;

import static org.assertj.core.api.Assertions.*;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.refrigerator.domain.RefrigeratorItem;
import com.capstone.web.refrigerator.dto.RefrigeratorDto;
import com.capstone.web.refrigerator.exception.DuplicateItemException;
import com.capstone.web.refrigerator.exception.ItemNotFoundException;
import com.capstone.web.refrigerator.exception.UnauthorizedItemAccessException;
import com.capstone.web.refrigerator.repository.RefrigeratorItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RefrigeratorServiceTest {

    @Autowired private RefrigeratorService refrigeratorService;
    @Autowired private RefrigeratorItemRepository refrigeratorItemRepository;
    @Autowired private MemberRepository memberRepository;

    private Member testMember;
    private Member otherMember;

    @BeforeEach
    void setup() {
        refrigeratorItemRepository.deleteAll();
        memberRepository.deleteAll();

        testMember = memberRepository.save(Member.builder()
                .email("test@test.com")
                .password("password")
                .nickname("테스터")
                .build());

        otherMember = memberRepository.save(Member.builder()
                .email("other@test.com")
                .password("password")
                .nickname("타인")
                .build());
    }

    // ========== REF-01: 냉장고 식재료 목록 조회 테스트 ==========

    @DisplayName("식재료 목록 조회 - 빈 목록")
    @Test
    void getMyItems_EmptyList() {
        // when
        RefrigeratorDto.ItemListResponse response = 
            refrigeratorService.getMyItems(testMember.getId(), "expirationDate");

        // then
        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotalCount()).isZero();
        assertThat(response.getExpiringCount()).isZero();
        assertThat(response.getExpiredCount()).isZero();
    }

    @DisplayName("식재료 목록 조회 - 소비기한순 정렬 (null은 마지막)")
    @Test
    void getMyItems_SortByExpirationDate() {
        // given
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("당근")
                .expirationDate(LocalDate.now().plusDays(10))
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("소금")  // 소비기한 없음
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("우유")
                .expirationDate(LocalDate.now().plusDays(2))
                .build());

        // when
        RefrigeratorDto.ItemListResponse response = 
            refrigeratorService.getMyItems(testMember.getId(), "expirationDate");

        // then
        assertThat(response.getItems()).hasSize(3);
        assertThat(response.getItems().get(0).getName()).isEqualTo("우유");
        assertThat(response.getItems().get(1).getName()).isEqualTo("당근");
        assertThat(response.getItems().get(2).getName()).isEqualTo("소금");
    }

    @DisplayName("식재료 목록 조회 - 이름순 정렬")
    @Test
    void getMyItems_SortByName() {
        // given
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("토마토")
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("계란")
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("사과")
                .build());

        // when
        RefrigeratorDto.ItemListResponse response = 
            refrigeratorService.getMyItems(testMember.getId(), "name");

        // then
        assertThat(response.getItems()).hasSize(3);
        assertThat(response.getItems().get(0).getName()).isEqualTo("계란");
        assertThat(response.getItems().get(1).getName()).isEqualTo("사과");
        assertThat(response.getItems().get(2).getName()).isEqualTo("토마토");
    }

    @DisplayName("식재료 목록 조회 - 통계 정보 확인")
    @Test
    void getMyItems_Statistics() {
        // given
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("임박상품")
                .expirationDate(LocalDate.now().plusDays(2))
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("경과상품")
                .expirationDate(LocalDate.now().minusDays(1))
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("정상상품")
                .expirationDate(LocalDate.now().plusDays(10))
                .build());

        // when
        RefrigeratorDto.ItemListResponse response = 
            refrigeratorService.getMyItems(testMember.getId(), "expirationDate");

        // then
        assertThat(response.getTotalCount()).isEqualTo(3);
        assertThat(response.getExpiringCount()).isEqualTo(1);
        assertThat(response.getExpiredCount()).isEqualTo(1);
    }

    @DisplayName("식재료 목록 조회 - 다른 사용자 데이터는 조회되지 않음")
    @Test
    void getMyItems_OnlyOwnItems() {
        // given
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("내 우유")
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(otherMember)
                .name("타인 우유")
                .build());

        // when
        RefrigeratorDto.ItemListResponse response = 
            refrigeratorService.getMyItems(testMember.getId(), "name");

        // then
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getName()).isEqualTo("내 우유");
    }

    // ========== REF-02: 식재료 추가 테스트 ==========

    @DisplayName("식재료 추가 - 성공")
    @Test
    void addItem_Success() {
        // given
        RefrigeratorDto.CreateRequest request = RefrigeratorDto.CreateRequest.builder()
                .name("우유")
                .quantity(1)
                .unit("L")
                .expirationDate(LocalDate.now().plusDays(7))
                .memo("서울우유")
                .build();

        // when
        RefrigeratorDto.Response response = refrigeratorService.addItem(testMember.getId(), request);

        // then
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("우유");
        assertThat(response.getQuantity()).isEqualTo(1);
        assertThat(response.getUnit()).isEqualTo("L");
        assertThat(response.getExpirationDate()).isEqualTo(request.getExpirationDate());
        assertThat(response.getMemo()).isEqualTo("서울우유");
        assertThat(response.getDaysUntilExpiration()).isEqualTo(7);
        assertThat(response.isExpirationSoon()).isFalse();
        assertThat(response.isExpired()).isFalse();
    }

    @DisplayName("식재료 추가 - 중복된 이름으로 추가 시 예외")
    @Test
    void addItem_Fail_Duplicate() {
        // given
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("우유")
                .build());

        RefrigeratorDto.CreateRequest request = RefrigeratorDto.CreateRequest.builder()
                .name("우유")
                .quantity(2)
                .build();

        // when & then
        assertThatThrownBy(() -> refrigeratorService.addItem(testMember.getId(), request))
                .isInstanceOf(DuplicateItemException.class)
                .hasMessageContaining("이미 등록된 식재료");
    }

    @DisplayName("식재료 추가 - 소비기한 없이 추가 가능")
    @Test
    void addItem_Success_WithoutExpirationDate() {
        // given
        RefrigeratorDto.CreateRequest request = RefrigeratorDto.CreateRequest.builder()
                .name("소금")
                .quantity(500)
                .unit("g")
                .build();

        // when
        RefrigeratorDto.Response response = refrigeratorService.addItem(testMember.getId(), request);

        // then
        assertThat(response.getExpirationDate()).isNull();
        assertThat(response.getDaysUntilExpiration()).isNull();
        assertThat(response.isExpirationSoon()).isFalse();
        assertThat(response.isExpired()).isFalse();
    }

    // ========== REF-03,04: 식재료 일괄 추가 테스트 ==========

    @DisplayName("식재료 일괄 추가 - 모두 성공")
    @Test
    void addItemsBulk_AllSuccess() {
        // given
        List<RefrigeratorDto.CreateRequest> items = List.of(
                RefrigeratorDto.CreateRequest.builder()
                        .name("우유")
                        .quantity(1)
                        .unit("L")
                        .build(),
                RefrigeratorDto.CreateRequest.builder()
                        .name("계란")
                        .quantity(10)
                        .unit("개")
                        .build(),
                RefrigeratorDto.CreateRequest.builder()
                        .name("빵")
                        .quantity(1)
                        .unit("개")
                        .build()
        );

        RefrigeratorDto.BulkCreateRequest request = new RefrigeratorDto.BulkCreateRequest(items);

        // when
        RefrigeratorDto.BulkCreateResponse response = 
            refrigeratorService.addItemsBulk(testMember.getId(), request);

        // then
        assertThat(response.getSuccessCount()).isEqualTo(3);
        assertThat(response.getFailCount()).isZero();
        assertThat(response.getAddedItems()).hasSize(3);
        assertThat(response.getFailedItems()).isEmpty();
    }

    @DisplayName("식재료 일괄 추가 - 부분 성공 (중복 건너뛰기)")
    @Test
    void addItemsBulk_PartialSuccess() {
        // given
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("우유")
                .build());

        List<RefrigeratorDto.CreateRequest> items = List.of(
                RefrigeratorDto.CreateRequest.builder()
                        .name("우유")  // 중복
                        .quantity(1)
                        .build(),
                RefrigeratorDto.CreateRequest.builder()
                        .name("계란")  // 성공
                        .quantity(10)
                        .build(),
                RefrigeratorDto.CreateRequest.builder()
                        .name("빵")  // 성공
                        .quantity(1)
                        .build()
        );

        RefrigeratorDto.BulkCreateRequest request = new RefrigeratorDto.BulkCreateRequest(items);

        // when
        RefrigeratorDto.BulkCreateResponse response = 
            refrigeratorService.addItemsBulk(testMember.getId(), request);

        // then
        assertThat(response.getSuccessCount()).isEqualTo(2);
        assertThat(response.getFailCount()).isEqualTo(1);
        assertThat(response.getAddedItems()).hasSize(2);
        assertThat(response.getFailedItems()).hasSize(1);
        assertThat(response.getFailedItems().get(0)).contains("우유");
    }

    @DisplayName("식재료 일괄 추가 - 빈 목록")
    @Test
    void addItemsBulk_EmptyList() {
        // given
        RefrigeratorDto.BulkCreateRequest request = 
            new RefrigeratorDto.BulkCreateRequest(List.of());

        // when
        RefrigeratorDto.BulkCreateResponse response = 
            refrigeratorService.addItemsBulk(testMember.getId(), request);

        // then
        assertThat(response.getSuccessCount()).isZero();
        assertThat(response.getFailCount()).isZero();
        assertThat(response.getAddedItems()).isEmpty();
        assertThat(response.getFailedItems()).isEmpty();
    }

    // ========== REF-05: 식재료 수정 테스트 ==========

    @DisplayName("식재료 수정 - 성공")
    @Test
    void updateItem_Success() {
        // given
        RefrigeratorItem saved = refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("우유")
                .quantity(1)
                .unit("L")
                .expirationDate(LocalDate.now().plusDays(7))
                .build());

        RefrigeratorDto.UpdateRequest request = RefrigeratorDto.UpdateRequest.builder()
                .quantity(2)
                .unit("L")
                .expirationDate(LocalDate.now().plusDays(10))
                .memo("추가 구매")
                .build();

        // when
        RefrigeratorDto.Response response = 
            refrigeratorService.updateItem(testMember.getId(), saved.getId(), request);

        // then
        assertThat(response.getName()).isEqualTo("우유");  // 이름은 불변
        assertThat(response.getQuantity()).isEqualTo(2);
        assertThat(response.getUnit()).isEqualTo("L");
        assertThat(response.getExpirationDate()).isEqualTo(request.getExpirationDate());
        assertThat(response.getMemo()).isEqualTo("추가 구매");
    }

    @DisplayName("식재료 수정 - 존재하지 않는 ID로 수정 시 예외")
    @Test
    void updateItem_Fail_NotFound() {
        // given
        Long nonExistentId = 999L;

        RefrigeratorDto.UpdateRequest request = RefrigeratorDto.UpdateRequest.builder()
                .quantity(2)
                .build();

        // when & then
        assertThatThrownBy(() -> refrigeratorService.updateItem(testMember.getId(), nonExistentId, request))
                .isInstanceOf(ItemNotFoundException.class);
    }

    @DisplayName("식재료 수정 - 권한 없는 사용자가 수정 시 예외")
    @Test
    void updateItem_Fail_Unauthorized() {
        // given
        RefrigeratorItem saved = refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("우유")
                .build());

        RefrigeratorDto.UpdateRequest request = RefrigeratorDto.UpdateRequest.builder()
                .quantity(2)
                .build();

        // when & then
        assertThatThrownBy(() -> refrigeratorService.updateItem(otherMember.getId(), saved.getId(), request))
                .isInstanceOf(UnauthorizedItemAccessException.class)
                .hasMessageContaining("권한이 없습니다");
    }

    // ========== REF-06: 식재료 삭제 테스트 ==========

    @DisplayName("식재료 삭제 - 성공")
    @Test
    void deleteItem_Success() {
        // given
        RefrigeratorItem saved = refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("우유")
                .build());

        // when
        refrigeratorService.deleteItem(testMember.getId(), saved.getId());

        // then
        assertThat(refrigeratorItemRepository.existsById(saved.getId())).isFalse();
    }

    @DisplayName("식재료 삭제 - 존재하지 않는 ID로 삭제 시 예외")
    @Test
    void deleteItem_Fail_NotFound() {
        // given
        Long nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> refrigeratorService.deleteItem(testMember.getId(), nonExistentId))
                .isInstanceOf(ItemNotFoundException.class);
    }

    @DisplayName("식재료 삭제 - 권한 없는 사용자가 삭제 시 예외")
    @Test
    void deleteItem_Fail_Unauthorized() {
        // given
        RefrigeratorItem saved = refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("우유")
                .build());

        // when & then
        assertThatThrownBy(() -> refrigeratorService.deleteItem(otherMember.getId(), saved.getId()))
                .isInstanceOf(UnauthorizedItemAccessException.class)
                .hasMessageContaining("권한이 없습니다");
    }
}
