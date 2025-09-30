package com.back.domain.member;

import com.back.domain.item.entity.Item;
import com.back.domain.item.entity.ItemType;
import com.back.domain.item.repository.ItemRepository;
import com.back.domain.member.controller.ApiV1MemberController;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.title.entity.Title;
import com.back.domain.title.repository.TitleRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private MockMvc mvc;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MemberService memberService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private TitleRepository titleRepository;

    private Member user1;
    private Item item1, item2;
    private Title title1, title2;

    @BeforeEach
    void setUp() {
        item1 = itemRepository.save(new Item(ItemType.AVATAR, "아이템1", ""));
        item2 = itemRepository.save(new Item(ItemType.AVATAR, "아이템2", ""));
        title1 = titleRepository.save(new Title("칭호1"));
        title2 = titleRepository.save(new Title("칭호2"));

        user1 = memberService.signup(
                "test1@test.com",
                "test123",
                "테스트유저1"
        );

        user1.addItem(item1);
        user1.addItem(item2);
        user1.setItem(item1);

        user1.addTitle(title1);
        user1.addTitle(title2);
        user1.setTitle(title1);
    }

    @Test
    @DisplayName("회원 가입(일반 계정)")
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
        assertNotNull(member);

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("signup"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201"))
                .andExpect(jsonPath("$.message").value("[Member] Success: 회원 가입"))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.id").value(member.getId()));
    }

    @Test
    @DisplayName("로그인(일반 계정)")
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
                .andExpect(jsonPath("$.content.item.id").value(user1.getId()))
                .andExpect(jsonPath("$.content.apiKey").value(user1.getApiKey()))
                .andExpect(jsonPath("$.content.accessToken").isNotEmpty());

        resultActions.andExpect(
                result -> {
                    Cookie apiKeyCookie = result.getResponse().getCookie("apiKey");
                    assertNotNull(apiKeyCookie);
                    assertThat(apiKeyCookie.getValue()).isEqualTo(user1.getApiKey());
                    assertThat(apiKeyCookie.getPath()).isEqualTo("/");
                    assertThat(apiKeyCookie.getAttribute("HttpOnly")).isEqualTo("true");

                    Cookie accessTokenCookie = result.getResponse().getCookie("accessToken");
                    assertNotNull(accessTokenCookie);
                    assertThat(accessTokenCookie.getValue()).isNotBlank();
                    assertThat(accessTokenCookie.getPath()).isEqualTo("/");
                    assertThat(accessTokenCookie.getAttribute("HttpOnly")).isEqualTo("true");
                }
        );
    }

    @Test
    @DisplayName("로그아웃")
    void logout() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        delete(baseUrl+ "/logout")
                                .cookie(new Cookie("apiKey", user1.getApiKey()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("[Member] Success: 로그아웃"))
                .andExpect(result -> {
                    Cookie apiKeyCookie = result.getResponse().getCookie("apiKey");
                    assertThat(apiKeyCookie.getValue()).isEmpty();
                    assertThat(apiKeyCookie.getMaxAge()).isEqualTo(0);
                    assertThat(apiKeyCookie.getPath()).isEqualTo("/");
                    assertThat(apiKeyCookie.isHttpOnly()).isTrue();

                    Cookie accessTokenCookie = result.getResponse().getCookie("accessToken");
                    assertThat(accessTokenCookie.getValue()).isEmpty();
                    assertThat(accessTokenCookie.getMaxAge()).isEqualTo(0);
                    assertThat(accessTokenCookie.getPath()).isEqualTo("/");
                    assertThat(accessTokenCookie.isHttpOnly()).isTrue();
                });
    }

    @Test
    @DisplayName("가입 완료 검사")
    void valid_check() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get(baseUrl + "/valid")
                                .cookie(new Cookie("apiKey", user1.getApiKey()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("valid_check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("[Member] Success: 가입 완료 검사"))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.valid").value(false));
    }

    @Test
    @DisplayName("가입 완료 처리")
    void valid_set() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        put(baseUrl + "/valid")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "유저1완료",
                                            "birth": "2050-10-20",
                                            "gender": "FEMALE"
                                        }
                                        """.stripIndent())
                                .cookie(new Cookie("apiKey", user1.getApiKey()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("valid_set"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("[Member] Success: 가입 완료 처리"))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.name").value(user1.getName()))
                .andExpect(jsonPath("$.content.birth").value(user1.getBirth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).substring(0, 10)))
                .andExpect(jsonPath("$.content.gender").value(user1.getGender().name()))
                .andExpect(jsonPath("$.content.code").value(notNullValue()));
    }

    @Test
    @DisplayName("회원 정보 수정")
    void modifyProfile() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        put(baseUrl + "/modify/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "유저1수정",
                                            "birth": "2050-10-20",
                                            "gender": "FEMALE"
                                        }
                                        """.stripIndent())
                                .cookie(new Cookie("apiKey", user1.getApiKey()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("modifyProfile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("[Member] Success: 회원 정보 수정"))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.name").value(user1.getName()))
                .andExpect(jsonPath("$.content.birth").value(user1.getBirth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).substring(0, 10)))
                .andExpect(jsonPath("$.content.gender").value(user1.getGender().name()));
    }

    @Test
    @DisplayName("비밀번호 변경(일반 계정)")
    void modifyPassword() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        put(baseUrl + "/modify/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "password": "test123mod"
                                        }
                                        """.stripIndent())
                                .cookie(new Cookie("apiKey", user1.getApiKey()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("modifyPassword"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("[Member] Success: 비밀번호 변경"));

        resultActions.andExpect(
                result -> {
                    assertThat(passwordEncoder.matches("test123mod", user1.getPassword())).isTrue();
                }
        );
    }

    @Test
    @DisplayName("회원 정보 확인")
    void me() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get(baseUrl + "/me")
                                .cookie(new Cookie("apiKey", user1.getApiKey()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("[Member] Success: 사용자 정보 확인 (%s)".formatted(user1.getEmail())))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.id").value(user1.getId()));
    }

    @Test
    @DisplayName("보유한 아이템 정보 확인")
    void myItems() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get(baseUrl + "/me/items")
                                .cookie(new Cookie("apiKey", user1.getApiKey()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("myItems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("[Member] Success: 보유한 아이템 확인"))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    int count = 0;
                    for (ItemType type : ItemType.values()) {
                        int size = JsonPath.read(json, "$.content.items." + type.name() + ".length()");
                        count += size;
                    }
                    assertEquals(user1.getOwnedItems().size(), count);
                });
    }

    @Test
    @DisplayName("보유한 칭호 정보 확인")
    void myTitles() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get(baseUrl + "/me/titles")
                                .cookie(new Cookie("apiKey", user1.getApiKey()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("myTitles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("[Member] Success: 보유한 칭호 확인"))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.titles.length()").value(user1.getOwnedTitles().size()));
    }

    @Test
    @DisplayName("아이템 장착")
    void equipItem() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        put(baseUrl + "/equip/item/" + item2.getId())
                                .cookie(new Cookie("apiKey", user1.getApiKey()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("equipItem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("[Member] Success: 아이템 장착"))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.items." + item2.getType()).value(item2.getId()));
    }

    @Test
    @DisplayName("칭호 장착")
    void equipTitle() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        put(baseUrl + "/equip/title/" + title2.getId())
                                .cookie(new Cookie("apiKey", user1.getApiKey()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("equipTitle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("[Member] Success: 칭호 장착"))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.title").value(title2.getId()));
    }

    @Test
    @DisplayName("회원 탈퇴")
    void del() throws Exception {
        String email = user1.getEmail();

        ResultActions resultActions = mvc
                .perform(
                        delete(baseUrl + "/delete")
                                .cookie(new Cookie("apiKey", user1.getApiKey()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("[Member] Success: 회원 탈퇴 (%s)".formatted(email)));
    }
}
