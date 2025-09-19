package com.back.domain.member.controller;

import com.back.domain.member.dto.LoginReqDto;
import com.back.domain.member.dto.LoginResDto;
import com.back.domain.member.dto.MemberDto;
import com.back.domain.member.dto.SignupReqDto;
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
    @Operation(summary = "회원가입", description = "회원 가입 API")
    public ResponseEntity<ApiResponse<MemberDto>> signup(
            @Valid @RequestBody SignupReqDto reqBody
    ) {
        Member member = memberService.signup(
                reqBody.email(),
                reqBody.password(),
                reqBody.name(),
                reqBody.age(),
                reqBody.gender()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "201",
                        "회원가입 성공",
                        new MemberDto(member)
                        )
                );
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "회원 로그인 API")
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
                        "로그인 성공",
                        new LoginResDto(
                                new MemberDto(member),
                                member.getApiKey(),
                                accessToken
                        ))
                );
    }

    @DeleteMapping("/logout")
    @Operation(summary = "로그아웃", description = "회원 로그아웃 API")
    public ResponseEntity<ApiResponse<Void>> logout() {
        rq.deleteCookie("apiKey");
        rq.deleteCookie("accessToken");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        "200",
                        "로그아웃 성공"
                        )
                );
    }

    //테스트용. 삭제 예정
    @GetMapping("/test")
    @Operation(summary = "로그인 테스트 (삭제 예정)", description = "test API")
    public ResponseEntity<ApiResponse<Void>> test() {
        Member actor = rq.getActor();

        if (actor == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(
                            "404",
                            "로그인 실패"
                            )
                    );
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        "200",
                        "로그인 된 유저: %s".formatted(actor.getEmail())
                        )
                );
    }
}
