package com.back.domain.member.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.MemberGender;
import com.back.domain.member.repository.MemberRepository;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final AuthService authService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    //가입
    public Member signup(
            String email,
            String password,
            String name,
            int age,
            MemberGender gender
    ) {
        memberRepository
                .findByEmail(email)
                .ifPresent(_member -> {
                    throw new CustomException(ErrorCode.CONFLICT, "이미 가입된 이메일입니다.");
                });

        password = passwordEncoder.encode(password);
        Member member = new Member(email, password, name, age, gender);

        return memberRepository.save(member);
    }

    //로그인
    public Member login(String email, String password) {
        Member member = findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED, "잘못된 이메일입니다."));
        if(!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "잘못된 비밀번호입니다.");
        }

        return member;
    }

    // *** Find 메서드 ***
    public Optional<Member> findById(int id) {
        return memberRepository.findById(id);
    }

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public Optional<Member> findByApiKey(String apiKey) {
        return memberRepository.findByApiKey(apiKey);
    }

    // *** Modify 메서드 ***
    public void modify() {

    }

    // *** 인증/인가 관련 메서드 ***
    public String genAccessToken(Member member) {
        return authService.genAccessToken(member);
    }

    public Map<String, Object> payload(String accessToken) {
        return authService.payload(accessToken);
    }
}
