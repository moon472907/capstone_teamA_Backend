package com.back.global.security;

import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import com.back.global.rq.Rq;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {
    private final MemberService memberService;
    private final Rq rq;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            work(request, response, filterChain);
        } catch (CustomException e) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(e.getHttpStatus().value());
        }
    }

    private void work(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (List.of("/api/v1/members/signup", "/api/v1/members/login").contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = resolveToken(request);

        if (accessToken == null || accessToken.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        Map<String, Object> payload = memberService.payload(accessToken);
        if (payload == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "[Security] Fail: 유효하지 않은 토큰");
        }

        int id = (int) payload.get("id");
        String email = (String) payload.get("email");
        Member member = new Member(id, email);

        UserDetails user = new SecurityUser(
                member.getId(),
                member.getEmail(),
                "",
                member.getAuthorities()
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                user.getPassword(),
                user.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = rq.getHeader("Authorization", "");
        if (!header.isBlank()) {
            if (!header.startsWith("Bearer "))
                throw new CustomException(ErrorCode.UNAUTHORIZED, "[Security] Fail: Authorization 헤더가 Bearer 형식이 아님");
            return header.substring(7).trim();
        }
        return rq.getCookieValue("accessToken", "");
    }
}
