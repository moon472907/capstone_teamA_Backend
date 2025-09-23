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

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final AuthService authService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    //가입 (일반)
    public Member signup(
            String email,
            String password,
            String name
    ) {
        findByEmail(email)
                .ifPresent(_member -> {
                    throw new CustomException(ErrorCode.CONFLICT, "이미 가입된 계정입니다.");
                });

        password = passwordEncoder.encode(password);
        Member member = new Member(email, password, name);

        return memberRepository.save(member);
    }

    //가입or로그인 (소셜 계정)
    public Member social_login(String email, String password, String name) {
        Member member = findByEmail(email).orElse(null);

        if(member == null) {
            member = signup(email, password, name);
        }
        else {
            modifyName(member, name);
        }

        return member;
    }

    //로그인 (일반)
    public Member login(String email, String password) {
        Member member = findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED, "잘못된 이메일입니다."));
        if(!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "잘못된 비밀번호입니다.");
        }

        return member;
    }

    //식별코드 생성
    public void genCode(Member member) {
        final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        final SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder(6);
        do {
            for(int i=0; i<6; i++) {
                sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
            }
        } while(memberRepository.existsByCode(sb.toString()));

        member.setCode(sb.toString());
    }

    //회원 탈퇴
    public void delete(Member member) {
        memberRepository.delete(member);
    }

    // *** Modify 메서드 ***
    public void modifyName(Member member, String name) {
        member.setName(name);
    }

    public void modifyPassword(Member member, String password) {
        member.setPassword(passwordEncoder.encode(password));
    }

    public void modifyProfile(Member member, LocalDate age, MemberGender gender) {
        member.setBirth(age);
        member.setGender(gender);
    }

    public void modifyStatus(
            Member member,
            int level,
            int xp,
            int money
    ) {
        member.setLevel(level);
        member.setXp(xp);
        member.setMoney(money);
    }

    // *** Find 메서드 ***
    public Optional<Member> findById(int id) {
        return memberRepository.findById(id);
    }

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public Optional<Member> findByCode(String code) {
        return memberRepository.findByCode(code);
    }

    public Optional<Member> findByApiKey(String apiKey) {
        return memberRepository.findByApiKey(apiKey);
    }

    // *** 인증/인가 관련 메서드 ***
    public String genAccessToken(Member member) {
        return authService.genAccessToken(member);
    }

    public Map<String, Object> payload(String accessToken) {
        return authService.payload(accessToken);
    }
}
