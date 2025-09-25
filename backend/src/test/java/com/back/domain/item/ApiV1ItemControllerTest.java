package com.back.domain.item;

import com.back.domain.item.dto.CreateItemDto;
import com.back.domain.item.entity.Item;
import com.back.domain.item.entity.ItemType;
import com.back.domain.item.repository.ItemRepository;
import com.back.domain.item.service.ItemService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@DisplayName("아이템 API CRUD 통합 테스트")
public class ApiV1ItemControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EntityManager em;
    @Autowired
    ItemService itemService;

    @Autowired
    ItemRepository itemRepository;
    private String baseUrl;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {

        baseUrl = "http://localhost:" + port + "/api/v1/item";
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("READ - 전체 알림 조회 성공")
    void findAllItems_Success() {
        CreateItemDto createItemDto1 = new CreateItemDto(
                "테스트1", ItemType.AVATAR);
        CreateItemDto createItemDto2 = new CreateItemDto(
                "테스트2", ItemType.FURNITURE);
        CreateItemDto createItemDto3 = new CreateItemDto(
                "테스트3", ItemType.FURNITURE);

        HttpEntity<CreateItemDto> request1 = new HttpEntity<>(createItemDto1, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request1, String.class);

        HttpEntity<CreateItemDto> request2 = new HttpEntity<>(createItemDto2, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request2, String.class);

        HttpEntity<CreateItemDto> request3 = new HttpEntity<>(createItemDto3, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request3, String.class);

        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("아이템 전체 조회 성공");
        assertThat(response.getBody()).contains("테스트1");
        assertThat(response.getBody()).contains("테스트2");
        assertThat(response.getBody()).contains("테스트3");
    }


    @Test
    @DisplayName("READ - 알림 단건 조회 성공")
    void findItemsByID_Success()
    {
        List<Item> itemList =  itemRepository.findAll();
        int num  = 0;
        if (itemList.size() != 0) {
            num = itemList.getLast().getId();
        }
        CreateItemDto createItemDto1 = new CreateItemDto(
                "테스트1", ItemType.AVATAR);
        CreateItemDto createItemDto2 = new CreateItemDto(
                "테스트2", ItemType.FURNITURE);
        CreateItemDto createItemDto3 = new CreateItemDto(
                "테스트3", ItemType.FURNITURE);

        HttpEntity<CreateItemDto> request1 = new HttpEntity<>(createItemDto1, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request1, String.class);

        HttpEntity<CreateItemDto> request2 = new HttpEntity<>(createItemDto2, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request2, String.class);

        HttpEntity<CreateItemDto> request3 = new HttpEntity<>(createItemDto3, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request3, String.class);

        num++;
        ResponseEntity<String> response1 = restTemplate.getForEntity(baseUrl+"/"+num, String.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response1.getBody()).contains("테스트1");
        assertThat(response1.getBody()).contains(ItemType.AVATAR.toString());

        num++;
        ResponseEntity<String> response2 = restTemplate.getForEntity(baseUrl+"/"+num, String.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).contains("테스트2");
        assertThat(response2.getBody()).contains(ItemType.FURNITURE.toString());

        num++;
        ResponseEntity<String> response3 = restTemplate.getForEntity(baseUrl+"/"+num, String.class);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response3.getBody()).contains("테스트3");
        assertThat(response3.getBody()).contains(ItemType.FURNITURE.toString());
    }



    @Test
    @DisplayName("READ - 알림 타입별 조회 성공")
    void findItemsByItemType_Success()
    {
        CreateItemDto createItemDto1 = new CreateItemDto(
                "테스트1", ItemType.AVATAR);
        CreateItemDto createItemDto2 = new CreateItemDto(
                "테스트2", ItemType.FURNITURE);
        CreateItemDto createItemDto3 = new CreateItemDto(
                "테스트3", ItemType.FURNITURE);

        HttpEntity<CreateItemDto> request1 = new HttpEntity<>(createItemDto1, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request1, String.class);

        HttpEntity<CreateItemDto> request2 = new HttpEntity<>(createItemDto2, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request2, String.class);

        HttpEntity<CreateItemDto> request3 = new HttpEntity<>(createItemDto3, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request3, String.class);



        ResponseEntity<String> response1 = restTemplate.getForEntity(baseUrl+"/ItemType/AVATAR", String.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response1.getBody()).contains("테스트1");
        assertThat(response1.getBody()).contains(ItemType.AVATAR.toString());
        assertThat(response1.getBody()).doesNotContain(ItemType.FURNITURE.toString());
        assertThat(response1.getBody()).doesNotContain(ItemType.CLOTHE.toString());


        ResponseEntity<String> response2 = restTemplate.getForEntity(baseUrl+"/ItemType/FURNITURE", String.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).contains("테스트2");
        assertThat(response2.getBody()).contains("테스트3");
        assertThat(response2.getBody()).contains(ItemType.FURNITURE.toString());
        assertThat(response2.getBody()).doesNotContain(ItemType.AVATAR.toString());
        assertThat(response2.getBody()).doesNotContain(ItemType.CLOTHE.toString());





    }

    @Test
    @DisplayName("UPDATE - 알림 수정 성공")
    void updateItem_Success()
    {
        List<Item> itemList =  itemRepository.findAll();
        int num  = 0;
        if (itemList.size() != 0) {
            num = itemList.getLast().getId();
        }
        CreateItemDto createItemDto1 = new CreateItemDto(
                "테스트1", ItemType.AVATAR);
        CreateItemDto createItemDto2 = new CreateItemDto(
                "테스트2", ItemType.FURNITURE);
        CreateItemDto createItemDto3 = new CreateItemDto(
                "테스트3", ItemType.FURNITURE);

        HttpEntity<CreateItemDto> request1 = new HttpEntity<>(createItemDto1, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request1, String.class);

        HttpEntity<CreateItemDto> request2 = new HttpEntity<>(createItemDto2, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request2, String.class);

        HttpEntity<CreateItemDto> request3 = new HttpEntity<>(createItemDto3, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request3, String.class);

        num++;
        CreateItemDto updateDto1 = new CreateItemDto(
                "업데이트1", ItemType.CLOTHE);
        HttpEntity<CreateItemDto> updateRequest1 = new HttpEntity<>(updateDto1, headers);
        ResponseEntity<String> response1 = restTemplate.exchange(baseUrl+"/"+num, HttpMethod.PUT, updateRequest1, String.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response1.getBody()).contains("업데이트1");
        assertThat(response1.getBody()).contains(ItemType.CLOTHE.toString());
        assertThat(response1.getBody()).doesNotContain("테스트1");
        assertThat(response1.getBody()).doesNotContain(ItemType.AVATAR.toString());

        num++;
        CreateItemDto updateDto2 = new CreateItemDto(
                "업데이트2", ItemType.CLOTHE);
        HttpEntity<CreateItemDto> updateRequest2 = new HttpEntity<>(updateDto2, headers);
        ResponseEntity<String> response2 = restTemplate.exchange(baseUrl+"/"+num, HttpMethod.PUT, updateRequest2, String.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).contains("업데이트2");
        assertThat(response2.getBody()).contains(ItemType.CLOTHE.toString());
        assertThat(response2.getBody()).doesNotContain("테스트2");
        assertThat(response2.getBody()).doesNotContain(ItemType.FURNITURE.toString());
    }


    private static final Logger log = LoggerFactory.getLogger(ApiV1ItemControllerTest.class);

}