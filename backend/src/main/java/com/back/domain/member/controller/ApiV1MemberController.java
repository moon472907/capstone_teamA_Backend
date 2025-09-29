package com.back.domain.member.controller;

import com.back.domain.member.dto.*;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.global.common.ApiResponse;
import com.back.global.rq.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "ApiV1MemberController", description = "API 회원 컨트롤러")
public class ApiV1MemberController {
    private final MemberService memberService;
    private final Rq rq;

    @PostMapping("/signup")
    @Operation(summary = "회원 가입(일반 계정)", description = "일반 계정(소셜 X) 회원 가입")
    public ResponseEntity<ApiResponse<MemberDto>> signup(
            @Valid @RequestBody SignupReqDto reqBody
    ) {
        Member member = memberService.signup(reqBody.email(), reqBody.password(), reqBody.name());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "201",
                        "[Member] Success: 회원 가입",
                        new MemberDto(member)
                        )
                );
    }

    @PostMapping("/login")
    @Operation(summary = "로그인(일반 계정)", description = "일반 계정(소셜 X) 로그인")
    public ResponseEntity<ApiResponse<LoginResDto>> login(
            @Valid @RequestBody LoginReqDto reqBody
    ) {
        Member member = memberService.login(reqBody.email(), reqBody.password());

        String accessToken = memberService.genAccessToken(member);
        rq.setCookie("apiKey", member.getApiKey());
        rq.setCookie("accessToken", accessToken);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        "200",
                        "[Member] Success: 로그인",
                        new LoginResDto(
                                new MemberDto(member),
                                member.getApiKey(),
                                accessToken
                        ))
                );
    }

    @DeleteMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃")
    public ResponseEntity<ApiResponse<Void>> logout() {
        rq.deleteCookie("apiKey");
        rq.deleteCookie("accessToken");
        rq.deleteCookie("JSESSIONID");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        "200",
                        "[Member] Success: 로그아웃"
                        )
                );
    }

    public record ValidResDto(boolean valid) {}
    @GetMapping("/valid")
    @Operation(summary = "가입 완료 검사", description = "가입 절차가 완료된 계정인지 확인")
    public ResponseEntity<ApiResponse<ValidResDto>> valid_check() {
        Member actor = rq.getActorFromDb();

        boolean valid = (actor.getCode() != null);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                                "200",
                                "[Member] Success: 가입 완료 검사",
                                new ValidResDto(valid)
                        )
                );
    }

    @PutMapping("/valid")
    @Operation(summary = "가입 완료 처리", description = "가입 절차 완료 처리")
    public ResponseEntity<ApiResponse<MemberDto>> valid_set(
            @Valid @RequestBody ModifyReqDto reqBody
    ) {
        Member actor = rq.getActorFromDb();
        memberService.modifyProfile(actor, reqBody.name(), reqBody.birth(), reqBody.gender());
        memberService.genCode(actor);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                                "200",
                                "[Member] Success: 가입 완료 처리",
                                new MemberDto(actor)
                        )
                );
    }

    @PutMapping("/modify/profile")
    @Operation(summary = "회원 정보 수정", description = "생년월일, 성별 수정")
    public ResponseEntity<ApiResponse<MemberDto>> modifyProfile(
            @Valid @RequestBody ModifyReqDto reqBody
    ) {
        Member actor = rq.getActorFromDb();
        memberService.modifyProfile(actor, reqBody.name(), reqBody.birth(), reqBody.gender());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        "200",
                        "[Member] Success: 회원 정보 수정",
                        new MemberDto(actor)
                        )
                );
    }

    public record PasswordReqDto(String password) {}
    @PutMapping("/modify/password")
    @Operation(summary = "비밀번호 변경(일반 계정)", description = "비밀번호 변경")
    public ResponseEntity<ApiResponse<Void>> modifyPassword(
            @Valid @RequestBody PasswordReqDto reqBody
    ) {
        Member actor = rq.getActorFromDb();
        memberService.modifyPassword(actor, reqBody.password());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                                "200",
                                "[Member] Success: 비밀번호 변경"
                        )
                );
    }

    @GetMapping("/me")
    @Operation(summary = "회원 정보 확인", description = "현재 로그인된 사용자 정보 확인")
    public ResponseEntity<ApiResponse<MemberDto>> me() {
        Member actor = rq.getActorFromDb();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        "200",
                        "[Member] Success: 사용자 정보 확인 (%s)".formatted(actor.getEmail()),
                        new MemberDto(actor)
                        )
                );
    }

    @PutMapping("/equip/item/{id}")
    @Operation(summary = "아이템 장착", description = "보유한 아이템 장착")
    public ResponseEntity<ApiResponse<MemberDto>> equipItem(
            @PathVariable String id
    ) {
        Member actor = rq.getActorFromDb();
        memberService.modifyItem(actor, Integer.parseInt(id));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                                "200",
                                "[Member] Success: 아이템 장착",
                                new MemberDto(actor)
                        )
                );
    }

    @PutMapping("/equip/title/{id}")
    @Operation(summary = "칭호 장착", description = "보유한 칭호 장착")
    public ResponseEntity<ApiResponse<MemberDto>> equipTitle(
            @PathVariable String id
    ) {
        Member actor = rq.getActorFromDb();
        memberService.modifyTitle(actor, Integer.parseInt(id));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                                "200",
                                "[Member] Success: 칭호 장착",
                                new MemberDto(actor)
                        )
                );
    }

    @DeleteMapping("/delete")
    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴")
    public ResponseEntity<ApiResponse<Void>> delete() {
        Member actor = rq.getActorFromDb();
        String email = actor.getEmail();

        if(actor.getSocialAccessToken() != null) {
            memberService.delete_social(actor);
        }
        memberService.delete(actor);
        rq.deleteCookie("apiKey");
        rq.deleteCookie("accessToken");
        rq.deleteCookie("JSESSIONID");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                                "200",
                                "[Member] Success: 회원 탈퇴 (%s)".formatted(email)
                        )
                );
    }
}
