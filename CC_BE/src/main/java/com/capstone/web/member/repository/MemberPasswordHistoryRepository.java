package com.capstone.web.member.repository;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.domain.MemberPasswordHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberPasswordHistoryRepository extends JpaRepository<MemberPasswordHistory, Long> {
    List<MemberPasswordHistory> findTop5ByMemberOrderByChangedAtDesc(Member member);
    List<MemberPasswordHistory> findByMemberOrderByChangedAtDesc(Member member);
    long countByMember(Member member);
}
