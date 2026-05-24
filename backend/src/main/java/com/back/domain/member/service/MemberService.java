package com.back.domain.member.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final MemberRepository memberRepository;

    public Member signup(String email, String password, String name) {
        findByEmail(email)
                .ifPresent(_member -> {
                    throw new CustomException(ErrorCode.CONFLICT, "[Member] Fail: 이미 가입된 계정");
                });

        password = passwordEncoder.encode(password);
        Member member = new Member(email, password, name);

        return memberRepository.save(member);
    }

    public Member login(String email, String password) {
        Member member = findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED, "[Member] Fail: 잘못된 이메일"));
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "[Member] Fail: 잘못된 비밀번호");
        }

        return member;
    }

    public void delete(Member member) {
        member.setEmail("[DELETED_%d]%s".formatted(member.getId(), member.getEmail()));
        memberRepository.saveAndFlush(member);
        memberRepository.delete(member);
    }

    public void genCode(Member member) {
        final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        final SecureRandom random = new SecureRandom();

        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
            }
            code = sb.toString();
        } while (memberRepository.existsByCode(code));

        member.setCode(code);
    }

    public void modifyProfile(Member member, String name, LocalDate birth) {
        member.setName(name);
        member.setBirth(birth);
    }

    public Optional<Member> findById(int id) {
        return memberRepository.findById(id);
    }

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public Optional<Member> findByCode(String code) {
        return memberRepository.findByCode(code);
    }

    public String genAccessToken(Member member) {
        return authService.genAccessToken(member);
    }

    public Map<String, Object> payload(String accessToken) {
        return authService.payload(accessToken);
    }

    public void invalidateTokens(int memberId) {
        authService.invalidateMemberTokens(memberId);
    }
}
