package com.back.domain.member;

import com.back.domain.member.controller.ApiV1MemberController;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("회원 API 테스트")
public class ApiV1MemberControllerTest {
    private final String baseUrl = "/api/v1/members";

    @Autowired
    private MemberService memberService;
    @Autowired
    private MockMvc mvc;

    private Member user1;

    @BeforeEach
    void setUp() {
        user1 = memberService.signup(
                "test1@test.com",
                "test123",
                "테스트유저1"
        );
    }

    @Test
    @DisplayName("회원 가입")
    void signup() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post(baseUrl + "/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "newuser@test.com",
                                            "password": "123",
                                            "name": "가입테스트"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        Member member = memberService.findByEmail("newuser@test.com").get();

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("signup"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201"))
                .andExpect(jsonPath("$.message").value("[Member] Success: 회원 가입"))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.name").value(member.getName()));
    }

    @Test
    @DisplayName("로그인")
    void login() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post(baseUrl + "/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "test1@test.com",
                                            "password": "test123"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("[Member] Success: 로그인"))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.item").exists())
                .andExpect(jsonPath("$.content.item.name").value(user1.getName()))
                .andExpect(jsonPath("$.content.apiKey").value(user1.getApiKey()))
                .andExpect(jsonPath("$.content.accessToken").isNotEmpty());

        resultActions.andExpect(
                result -> {
                    Cookie apiKeyCookie = result.getResponse().getCookie("apiKey");
                    assertThat(apiKeyCookie.getValue()).isEqualTo(user1.getApiKey());
                    assertThat(apiKeyCookie.getPath()).isEqualTo("/");
                    assertThat(apiKeyCookie.getAttribute("HttpOnly")).isEqualTo("true");

                    Cookie accessTokenCookie = result.getResponse().getCookie("accessToken");
                    assertThat(accessTokenCookie.getValue()).isNotBlank();
                    assertThat(accessTokenCookie.getPath()).isEqualTo("/");
                    assertThat(accessTokenCookie.getAttribute("HttpOnly")).isEqualTo("true");
                }
        );
    }
}
