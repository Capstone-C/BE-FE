package com.capstone.web.refrigerator.service;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.refrigerator.domain.RefrigeratorItem;
import com.capstone.web.refrigerator.dto.RefrigeratorDto;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RefrigeratorServicePolicyTest {

    @Autowired
    private RefrigeratorService refrigeratorService;
    @Autowired
    private RefrigeratorItemRepository refrigeratorItemRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    @BeforeEach
    void setup() {
        refrigeratorItemRepository.deleteAll();
        memberRepository.deleteAll();
        member = memberRepository.save(Member.builder()
                .email("policy@test.com")
                .password("pw")
                .nickname("policy")
                .build());
    }

    @DisplayName("동일 이름+동일 소비기한: 수량 병합")
    @Test
    void mergeSameNameSameExpiration() {
        LocalDate exp = LocalDate.now().plusDays(5);
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(member)
                .name("우유")
                .quantity(2)
                .expirationDate(exp)
                .build());

        RefrigeratorDto.CreateRequest req = RefrigeratorDto.CreateRequest.builder()
                .name("우유")
                .quantity(3)
                .expirationDate(exp)
                .build();

        var resp = refrigeratorService.addItem(member.getId(), req);
        assertThat(resp.getQuantity()).isEqualTo(5);
        List<RefrigeratorItem> all = refrigeratorItemRepository.findByMemberOrderByNameAsc(member);
        assertThat(all).hasSize(1);
    }

    @DisplayName("동일 이름+다른 소비기한: 별도 항목 생성")
    @Test
    void separateDifferentExpiration() {
        LocalDate exp1 = LocalDate.now().plusDays(5);
        LocalDate exp2 = LocalDate.now().plusDays(10);
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(member)
                .name("우유")
                .quantity(2)
                .expirationDate(exp1)
                .build());

        RefrigeratorDto.CreateRequest req = RefrigeratorDto.CreateRequest.builder()
                .name("우유")
                .quantity(3)
                .expirationDate(exp2)
                .build();

        var resp = refrigeratorService.addItem(member.getId(), req);
        assertThat(resp.getQuantity()).isEqualTo(3);
        List<RefrigeratorItem> all = refrigeratorItemRepository.findByMemberOrderByNameAsc(member);
        assertThat(all).hasSize(2);
    }

    @DisplayName("소비기한 null 동일: 병합")
    @Test
    void mergeNullExpiration() {
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(member)
                .name("소금")
                .quantity(100)
                .expirationDate(null)
                .build());

        RefrigeratorDto.CreateRequest req = RefrigeratorDto.CreateRequest.builder()
                .name("소금")
                .quantity(50)
                .build();

        var resp = refrigeratorService.addItem(member.getId(), req);
        assertThat(resp.getQuantity()).isEqualTo(150);
        assertThat(resp.getExpirationDate()).isNull();
    }

    @DisplayName("한쪽만 소비기한 null → 별도 항목")
    @Test
    void separateWhenOneNullExpiration() {
        LocalDate exp = LocalDate.now().plusDays(30);
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(member)
                .name("설탕")
                .quantity(100)
                .expirationDate(null)
                .build());

        RefrigeratorDto.CreateRequest req = RefrigeratorDto.CreateRequest.builder()
                .name("설탕")
                .quantity(50)
                .expirationDate(exp)
                .build();

        var resp = refrigeratorService.addItem(member.getId(), req);
        assertThat(resp.getQuantity()).isEqualTo(50);
        List<RefrigeratorItem> all = refrigeratorItemRepository.findByMemberOrderByNameAsc(member);
        assertThat(all).hasSize(2);
    }
}

