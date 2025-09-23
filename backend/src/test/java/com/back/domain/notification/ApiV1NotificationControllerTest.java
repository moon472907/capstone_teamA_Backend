package com.back.domain.notification;

import com.back.domain.notification.dto.CreateNotificationDto;
import com.back.domain.notification.dto.ModifyNotificationDto;
import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.service.NotificationService;
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
@DisplayName("알림 API CRUD 통합 테스트")
public class ApiV1NotificationControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    NotificationService notificationService;

    private String baseUrl;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/notification";
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("CREATE - 알림 생성 성공")
    void createNotification_Success() {
        // given
        CreateNotificationDto createDto = new CreateNotificationDto(
                 100, "알림 생성 테스트", NotificationType.MESSAGE
        );

        // when
        HttpEntity<CreateNotificationDto> request = new HttpEntity<>(createDto, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl, HttpMethod.POST, request, String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
         assertThat(response.getBody()).contains("200-1");
        assertThat(response.getBody()).contains("알림이 생성되었습니다.");
        assertThat(response.getBody()).contains("알림 생성 테스트");
        assertThat(response.getBody()).contains("MESSAGE");
    }


    @Test
    @DisplayName("CREATE - 유효하지 않은 데이터로 생성 실패")
    void createNotification_InvalidData() {
        // given - 빈 메시지로 생성 시도
        CreateNotificationDto invalidDto = new CreateNotificationDto(
                0, "", NotificationType.MESSAGE
        );

        // when
        HttpEntity<CreateNotificationDto> request = new HttpEntity<>(invalidDto, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl, HttpMethod.POST, request, String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    @DisplayName("READ - 전체 알림 조회 성공")
    void findAllNotifications_Success() {
        // given - 테스트 데이터 생성
        CreateNotificationDto createDto1 = new CreateNotificationDto(
                 100, "테스트 알림 1", NotificationType.MESSAGE
        );
        CreateNotificationDto createDto2 = new CreateNotificationDto(
                101, "테스트 알림 2", NotificationType.MESSAGE
        );

        // 테스트 알림 생성
        HttpEntity<CreateNotificationDto> request1 = new HttpEntity<>(createDto1, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request1, String.class);

        HttpEntity<CreateNotificationDto> request2 = new HttpEntity<>(createDto2, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request2, String.class);

        // when
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("알림 전체 조회 성공");
        assertThat(response.getBody()).contains("테스트 알림 1");
        assertThat(response.getBody()).contains("테스트 알림 2");
    }


//    @Test
//    @DisplayName("READ - 단건 알림 조회 성공")
//    void findNotificationById_Success() {
//        // given - 테스트 알림 생성
//
//        CreateNotificationDto createDto = new CreateNotificationDto(
//                100, "단건 조회 테스트", NotificationType.MESSAGE
//        );
//
//        // when
//        HttpEntity<CreateNotificationDto> request = new HttpEntity<>(createDto, headers);
//        ResponseEntity<String> response = restTemplate.exchange(
//                baseUrl, HttpMethod.POST, request, String.class
//        );
//        System.out.println(response.getBody());
//
//int testId = 1;
//        // when
//        ResponseEntity<String> response2 = restTemplate.getForEntity(
//                baseUrl + "/" + testId, String.class
//        );
//
//        // then
//        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response2.getBody()).contains("알림 전체 조회 성공");
//        assertThat(response2.getBody()).contains("단건 조회 테스트");
//    }
//

    @Test
    @DisplayName("READ - 존재하지 않는 알림 조회")
    void findNotificationById_NotFound() {
        // given
        int nonExistentId = 999;

        // when
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/" + nonExistentId, String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Test
    @DisplayName("UPDATE - 알림 수정 성공")
    void updateNotification_Success() {
        // given - 테스트 알림 생성
        CreateNotificationDto createDto = new CreateNotificationDto(
                 100, "수정 전 알림", NotificationType.MESSAGE
        );

        HttpEntity<CreateNotificationDto> createRequest = new HttpEntity<>(createDto, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, createRequest, String.class);

        int testId = 1;

        ModifyNotificationDto modifyDto = new ModifyNotificationDto(
                100, "수정된 알림 메시지", NotificationType.MESSAGE
        );

        // when
        HttpEntity<ModifyNotificationDto> request = new HttpEntity<>(modifyDto, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + testId, HttpMethod.PUT, request, String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("수정된 알림 메시지");
        assertThat(response.getBody()).contains("MESSAGE");
    }

    @Test
    @DisplayName("UPDATE - 알림 읽음 처리 성공")
    void markNotificationAsRead_Success() {
        // given - 테스트 알림 생성
        CreateNotificationDto createDto = new CreateNotificationDto(
                 100, "읽음 처리 테스트", NotificationType.MESSAGE
        );

        HttpEntity<CreateNotificationDto> createRequest = new HttpEntity<>(createDto, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, createRequest, String.class);

        int testId = 1;

        // when
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/read/" + testId, HttpMethod.PUT, request, String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("true"); // isRead = true
    }

    @Test
    @DisplayName("UPDATE - 존재하지 않는 알림 수정")
    void updateNotification_NotFound() {
        // given
        int nonExistentId = 999;
        ModifyNotificationDto modifyDto = new ModifyNotificationDto(
                100, "존재하지 않는 알림 수정", NotificationType.MESSAGE
        );

        // when
        HttpEntity<ModifyNotificationDto> request = new HttpEntity<>(modifyDto, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + nonExistentId, HttpMethod.PUT, request, String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("UPDATE - 유효하지 않은 데이터로 수정")
    void updateNotification_InvalidData() {
        // given
        CreateNotificationDto createDto = new CreateNotificationDto(
                 100, "수정 테스트", NotificationType.MESSAGE
        );

        HttpEntity<CreateNotificationDto> createRequest = new HttpEntity<>(createDto, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, createRequest, String.class);

        int testId = 1;

        ModifyNotificationDto invalidDto = new ModifyNotificationDto(
                0, "", null
        );

        // when
        HttpEntity<ModifyNotificationDto> request = new HttpEntity<>(invalidDto, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + testId, HttpMethod.PUT, request, String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("DELETE - 알림 삭제 성공")
    void deleteNotification_Success() {
        // given - 테스트 알림 생성
        CreateNotificationDto createDto = new CreateNotificationDto(
                 100, "삭제 테스트 알림", NotificationType.MESSAGE
        );

        HttpEntity<CreateNotificationDto> createRequest = new HttpEntity<>(createDto, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, createRequest, String.class);

        int testId = 1;

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + testId, HttpMethod.DELETE, new HttpEntity<>(headers), String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("알림 삭제 성공");

        // 삭제 확인 - 조회 시 에러 발생
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                baseUrl + "/" + testId, String.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("DELETE - 존재하지 않는 알림 삭제")
    void deleteNotification_NotFound() {
        // given
        int nonExistentId = 999;

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + nonExistentId, HttpMethod.DELETE, new HttpEntity<>(headers), String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}



