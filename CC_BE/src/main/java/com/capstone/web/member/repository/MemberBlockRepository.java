package com.capstone.web.member.repository;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.domain.MemberBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MemberBlockRepository extends JpaRepository<MemberBlock, Long> {
    boolean existsByBlockerAndBlocked(Member blocker, Member blocked);
    Optional<MemberBlock> findByBlockerAndBlocked(Member blocker, Member blocked);
    List<MemberBlock> findAllByBlockerOrderByCreatedAtDesc(Member blocker);
}
