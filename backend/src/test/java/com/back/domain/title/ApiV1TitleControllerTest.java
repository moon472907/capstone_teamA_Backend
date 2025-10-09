
package com.back.domain.title;

import com.back.domain.title.dto.CreateTitleDto;
import com.back.domain.title.dto.TitleDto;
import com.back.domain.title.entity.Title;
import com.back.domain.title.repository.TitleRepository;
import com.back.domain.title.service.TitleService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@DisplayName("칭호 API CRUD 통합 테스트")
public class ApiV1TitleControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EntityManager em;

    @Autowired
    TitleService titleService;

    @Autowired
    TitleRepository titleRepository;
    private String baseUrl;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {

        baseUrl = "http://localhost:" + port + "/api/v1/title";
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }


    @Test
    @DisplayName("READ - 전체 칭호 조회 성공")
    void findAllItems_Success() {

        CreateTitleDto createTitleDto1 = new CreateTitleDto("칭호 테스트 1");
        CreateTitleDto createTitleDto2 = new CreateTitleDto("칭호 테스트 2");
        CreateTitleDto createTitleDto3 = new CreateTitleDto("칭호 테스트 3");

        HttpEntity<CreateTitleDto> request1 = new HttpEntity<>(createTitleDto1, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request1, String.class);
        HttpEntity<CreateTitleDto> request2 = new HttpEntity<>(createTitleDto2, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request2, String.class);
        HttpEntity<CreateTitleDto> request3 = new HttpEntity<>(createTitleDto3, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request3, String.class);

        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("칭호 테스트 1");
        assertThat(response.getBody()).contains("칭호 테스트 2");
        assertThat(response.getBody()).contains("칭호 테스트 3");
    }


    @Test
    @DisplayName("READ - 칭호 단건 조회 성공")
    void findAllTitle_Success() {

        Title title = titleRepository.findAll().getLast();

        CreateTitleDto createTitleDto1 = new CreateTitleDto("칭호 테스트 1");
        HttpEntity<CreateTitleDto> request1 = new HttpEntity<>(createTitleDto1, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request1, String.class);
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/" + (title.getId() + 1), String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("칭호 테스트 1");
    }

    @Test
    @DisplayName("UPDATE - 칭호 수정  성공")
    void createTitle_Success() {

        int titleId = titleRepository.findAll().getLast().getId() + 1;
        CreateTitleDto createTitleDto = new CreateTitleDto("칭호 테스트 1");
        HttpEntity<CreateTitleDto> request = new HttpEntity<>(createTitleDto, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("칭호 테스트 1");

        TitleDto titleDto = new TitleDto(titleId, "칭호 수정 테스트 1");
        HttpEntity<TitleDto> request1 = new HttpEntity<>(titleDto, headers);
        ResponseEntity<String> response1 = restTemplate.exchange(baseUrl + "/" + titleId, HttpMethod.PUT, request1, String.class);

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response1.getBody()).contains("칭호 수정 테스트 1");
    }

    @Test
    @DisplayName("DELETE - 칭호 삭제 성공")
    void UpdateTitle_Success() {

        CreateTitleDto createTitleDto = new CreateTitleDto("칭호 테스트 1");
        HttpEntity<CreateTitleDto> request = new HttpEntity<>(createTitleDto, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("칭호 테스트 1");
    }

}