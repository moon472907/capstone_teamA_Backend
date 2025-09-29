package com.back.domain.member.service;

import com.back.domain.item.entity.Item;
import com.back.domain.item.repository.ItemRepository;
import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.MemberGender;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.title.entity.Title;
import com.back.domain.title.repository.TitleRepository;
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
    private final ItemRepository itemRepository;
    private final TitleRepository titleRepository;

    //가입 (일반)
    public Member signup(String email, String password, String name) {
        findByEmail(email)
                .ifPresent(_member -> {
                    throw new CustomException(ErrorCode.CONFLICT, "[Member] Fail: 이미 가입된 계정");
                });

        password = passwordEncoder.encode(password);
        Member member = new Member(email, password, name);

        return memberRepository.save(member);
    }

    //로그인 (일반)
    public Member login(String email, String password) {
        Member member = findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED, "[Member] Fail: 잘못된 이메일"));
        if(!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "[Member] Fail: 잘못된 비밀번호");
        }

        return member;
    }

    //로그인 (소셜 계정)
    public Member social_login(String email, String name, String socialAccessToken) {
        Member member = findByEmail(email).orElse(null);

        //최초 로그인일 경우 가입 처리
        if(member == null) {
            member = signup(email, "", name);
        }

        member.setSocialAccessToken(socialAccessToken);

        return member;
    }

    //식별코드 생성
    public void genCode(Member member) {
        final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        final SecureRandom random = new SecureRandom();

        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for(int i=0; i<6; i++) {
                sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
            }
            code = sb.toString();
        } while(memberRepository.existsByCode(code));

        member.setCode(code);
    }

    //회원 탈퇴
    public void delete(Member member) {
        memberRepository.delete(member);
    }

    public void delete_social(Member member) {
        String provider = member.getEmail().substring(1, member.getEmail().indexOf("]"));
        authService.delete_social(provider, member.getSocialAccessToken());
    }

    // *** Modify 메서드 ***
    public void modifyPassword(Member member, String password) {
        member.setPassword(passwordEncoder.encode(password));
    }

    public void modifyProfile(Member member, String name, LocalDate age, MemberGender gender) {
        member.setName(name);
        member.setBirth(age);
        member.setGender(gender);
    }

    public void modifyStatus(Member member, int level, int xp, int money) {
        member.setLevel(level);
        member.setXp(xp);
        member.setMoney(money);
    }

    public void modifyItem(Member member, int id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "[Member] Fail: 존재하지 않는 아이템"));
        member.setItem(item);
    }

    public void modifyTitle(Member member, int id) {
        Title title = titleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "[Member] Fail: 존재하지 않는 칭호"));
        member.setTitle(title);
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
