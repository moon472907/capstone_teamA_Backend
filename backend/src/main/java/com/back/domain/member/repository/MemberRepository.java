package com.back.domain.member.repository;

import com.back.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    Optional<Member> findById(int id);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByApiKey(String apiKey);
}
