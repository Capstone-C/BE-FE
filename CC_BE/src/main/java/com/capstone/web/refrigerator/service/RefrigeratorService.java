package com.capstone.web.refrigerator.service;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.refrigerator.domain.RefrigeratorItem;
import com.capstone.web.refrigerator.dto.RefrigeratorDto;
import com.capstone.web.refrigerator.exception.DuplicateItemException;
import com.capstone.web.refrigerator.exception.ItemNotFoundException;
import com.capstone.web.refrigerator.exception.UnauthorizedItemAccessException;
import com.capstone.web.refrigerator.repository.RefrigeratorItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 냉장고 식재료 서비스
 * REF-01: 내 냉장고 식재료 목록 조회
 * REF-02: 수동으로 식재료 추가
 * REF-05: 식재료 정보 수정
 * REF-06: 식재료 삭제
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefrigeratorService {

    private final RefrigeratorItemRepository refrigeratorItemRepository;
    private final MemberRepository memberRepository;

    /**
     * REF-01: 내 냉장고 식재료 목록 조회
     * 소비기한 임박순으로 정렬
     */
    public RefrigeratorDto.ItemListResponse getMyItems(Long memberId, String sortBy) {
        Member member = getMemberById(memberId);
        
        List<RefrigeratorItem> items;
        if ("name".equalsIgnoreCase(sortBy)) {
            items = refrigeratorItemRepository.findByMemberOrderByNameAsc(member);
        } else if ("createdAt".equalsIgnoreCase(sortBy)) {
            items = refrigeratorItemRepository.findByMemberOrderByCreatedAtDesc(member);
        } else {
            // 기본: 소비기한 임박순
            items = refrigeratorItemRepository.findByMemberOrderByExpirationDateAsc(member);
        }
        
        return RefrigeratorDto.ItemListResponse.builder()
                .items(items)
                .build();
    }

    /**
     * REF-02: 수동으로 식재료 추가
     */
    @Transactional
    public RefrigeratorDto.Response addItem(Long memberId, RefrigeratorDto.CreateRequest request) {
        Member member = getMemberById(memberId);
        
        // 중복 체크
        if (refrigeratorItemRepository.existsByMemberAndName(member, request.getName())) {
            throw new DuplicateItemException("이미 등록된 식재료입니다: " + request.getName());
        }
        
        RefrigeratorItem item = RefrigeratorItem.builder()
                .member(member)
                .name(request.getName())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .expirationDate(request.getExpirationDate())
                .memo(request.getMemo())
                .build();
        
        RefrigeratorItem savedItem = refrigeratorItemRepository.save(item);
        log.info("식재료 추가: memberId={}, itemName={}", memberId, request.getName());
        
        return new RefrigeratorDto.Response(savedItem);
    }

    /**
     * REF-03, 04: 일괄 추가 (OCR 결과 등록)
     */
    @Transactional
    public RefrigeratorDto.BulkCreateResponse addItemsBulk(Long memberId, RefrigeratorDto.BulkCreateRequest request) {
        Member member = getMemberById(memberId);
        
        List<RefrigeratorItem> addedItems = new ArrayList<>();
        List<String> failedItems = new ArrayList<>();
        
        for (RefrigeratorDto.CreateRequest itemRequest : request.getItems()) {
            try {
                // 중복 체크: 중복이면 건너뛰기 (에러 발생 안 함)
                if (refrigeratorItemRepository.existsByMemberAndName(member, itemRequest.getName())) {
                    failedItems.add(itemRequest.getName() + " (이미 등록됨)");
                    continue;
                }
                
                RefrigeratorItem item = RefrigeratorItem.builder()
                        .member(member)
                        .name(itemRequest.getName())
                        .quantity(itemRequest.getQuantity())
                        .unit(itemRequest.getUnit())
                        .expirationDate(itemRequest.getExpirationDate())
                        .memo(itemRequest.getMemo())
                        .build();
                
                RefrigeratorItem savedItem = refrigeratorItemRepository.save(item);
                addedItems.add(savedItem);
            } catch (Exception e) {
                log.warn("식재료 일괄 추가 실패: {}", itemRequest.getName(), e);
                failedItems.add(itemRequest.getName() + " (오류)");
            }
        }
        
        log.info("식재료 일괄 추가: memberId={}, success={}, fail={}", memberId, addedItems.size(), failedItems.size());
        
        return RefrigeratorDto.BulkCreateResponse.builder()
                .addedItems(addedItems)
                .failedItems(failedItems)
                .build();
    }

    /**
     * REF-05: 식재료 정보 수정
     */
    @Transactional
    public RefrigeratorDto.Response updateItem(Long memberId, Long itemId, RefrigeratorDto.UpdateRequest request) {
        RefrigeratorItem item = getItemById(itemId);
        
        // 권한 확인
        if (!item.getMember().getId().equals(memberId)) {
            throw new UnauthorizedItemAccessException("식재료 수정 권한이 없습니다");
        }
        
        item.update(request.getQuantity(), request.getUnit(), request.getExpirationDate(), request.getMemo());
        log.info("식재료 수정: memberId={}, itemId={}", memberId, itemId);
        
        return new RefrigeratorDto.Response(item);
    }

    /**
     * REF-06: 식재료 삭제
     */
    @Transactional
    public void deleteItem(Long memberId, Long itemId) {
        RefrigeratorItem item = getItemById(itemId);
        
        // 권한 확인
        if (!item.getMember().getId().equals(memberId)) {
            throw new UnauthorizedItemAccessException("식재료 삭제 권한이 없습니다");
        }
        
        refrigeratorItemRepository.delete(item);
        log.info("식재료 삭제: memberId={}, itemId={}", memberId, itemId);
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));
    }

    private RefrigeratorItem getItemById(Long itemId) {
        return refrigeratorItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("식재료를 찾을 수 없습니다: " + itemId));
    }
}
