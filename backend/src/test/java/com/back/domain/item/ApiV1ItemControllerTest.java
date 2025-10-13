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
    @DisplayName("READ - 전체 아이템 조회 성공")
    void findAllItems_Success() {

        CreateItemDto createItemDto1 = new CreateItemDto(
                "테스트1", ItemType.SPECIAL,10);
        CreateItemDto createItemDto2 = new CreateItemDto(
                "테스트2", ItemType.SPORTS,10);
        CreateItemDto createItemDto3 = new CreateItemDto(
                "테스트3", ItemType.SPORTS,10);

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
    @DisplayName("READ - 아이템 단건 조회 성공")
    void findItemsByID_Success() {

        List<Item> itemList = itemRepository.findAll();
        int num = 0;
        if (itemList.size() != 0) {
            num = itemList.getLast().getId();
        }
        CreateItemDto createItemDto1 = new CreateItemDto(
                "테스트1", ItemType.SPECIAL,10);
        CreateItemDto createItemDto2 = new CreateItemDto(
                "테스트2", ItemType.SPORTS,10);
        CreateItemDto createItemDto3 = new CreateItemDto(
                "테스트3", ItemType.SPORTS,10);

        HttpEntity<CreateItemDto> request1 = new HttpEntity<>(createItemDto1, headers);

        restTemplate.exchange(baseUrl, HttpMethod.POST, request1, String.class);

        HttpEntity<CreateItemDto> request2 = new HttpEntity<>(createItemDto2, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request2, String.class);

        HttpEntity<CreateItemDto> request3 = new HttpEntity<>(createItemDto3, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request3, String.class);

        num++;
        ResponseEntity<String> response1 = restTemplate.getForEntity(baseUrl + "/" + num, String.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response1.getBody()).contains("테스트1");
        assertThat(response1.getBody()).contains(ItemType.SPECIAL.toString());

        num++;
        ResponseEntity<String> response2 = restTemplate.getForEntity(baseUrl + "/" + num, String.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).contains("테스트2");
        assertThat(response2.getBody()).contains(ItemType.SPORTS.toString());

        num++;
        ResponseEntity<String> response3 = restTemplate.getForEntity(baseUrl + "/" + num, String.class);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response3.getBody()).contains("테스트3");
        assertThat(response3.getBody()).contains(ItemType.SPORTS.toString());
    }


    @Test
    @DisplayName("READ - 아이템 타입별 조회 성공")
    void findItemsByItemType_Success() {

        CreateItemDto createItemDto1 = new CreateItemDto(
                "테스트1", ItemType.SPECIAL,10);
        CreateItemDto createItemDto2 = new CreateItemDto(
                "테스트2", ItemType.SPORTS,10);
        CreateItemDto createItemDto3 = new CreateItemDto(
                "테스트3", ItemType.SPORTS,10);


        HttpEntity<CreateItemDto> request1 = new HttpEntity<>(createItemDto1, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request1, String.class);

        HttpEntity<CreateItemDto> request2 = new HttpEntity<>(createItemDto2, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request2, String.class);

        HttpEntity<CreateItemDto> request3 = new HttpEntity<>(createItemDto3, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request3, String.class);


        ResponseEntity<String> response1 = restTemplate.getForEntity(baseUrl + "/ItemType/SPECIAL", String.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response1.getBody()).contains("테스트1");
        assertThat(response1.getBody()).contains(ItemType.SPECIAL.toString());
        assertThat(response1.getBody()).doesNotContain(ItemType.SPORTS.toString());
        assertThat(response1.getBody()).doesNotContain(ItemType.FESTIVAL.toString());


        ResponseEntity<String> response2 = restTemplate.getForEntity(baseUrl + "/ItemType/SPORTS", String.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).contains("테스트2");
        assertThat(response2.getBody()).contains("테스트3");
        assertThat(response2.getBody()).contains(ItemType.SPORTS.toString());
        assertThat(response2.getBody()).doesNotContain(ItemType.SPECIAL.toString());
        assertThat(response2.getBody()).doesNotContain(ItemType.FESTIVAL.toString());


    }

    @Test
    @DisplayName("UPDATE - 아이템 수정 성공")
    void updateItem_Success() {

        List<Item> itemList = itemRepository.findAll();
        int num = 0;
        if (itemList.size() != 0) {
            num = itemList.getLast().getId();
        }
        CreateItemDto createItemDto1 = new CreateItemDto(
                "테스트1", ItemType.SPECIAL,10);
        CreateItemDto createItemDto2 = new CreateItemDto(
                "테스트2", ItemType.SPORTS,10);
        CreateItemDto createItemDto3 = new CreateItemDto(
                "테스트3", ItemType.SPORTS,10);

        HttpEntity<CreateItemDto> request1 = new HttpEntity<>(createItemDto1, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request1, String.class);

        HttpEntity<CreateItemDto> request2 = new HttpEntity<>(createItemDto2, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request2, String.class);

        HttpEntity<CreateItemDto> request3 = new HttpEntity<>(createItemDto3, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request3, String.class);

        num++;
        CreateItemDto updateDto1 = new CreateItemDto(
                "업데이트1", ItemType.FESTIVAL,10);
        HttpEntity<CreateItemDto> updateRequest1 = new HttpEntity<>(updateDto1, headers);
        ResponseEntity<String> response1 = restTemplate.exchange(baseUrl + "/" + num, HttpMethod.PUT, updateRequest1, String.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response1.getBody()).contains("업데이트1");
        assertThat(response1.getBody()).contains(ItemType.FESTIVAL.toString());
        assertThat(response1.getBody()).doesNotContain("테스트1");
        assertThat(response1.getBody()).doesNotContain(ItemType.SPECIAL.toString());

        num++;
        CreateItemDto updateDto2 = new CreateItemDto(
                "업데이트2", ItemType.FESTIVAL,10);
        HttpEntity<CreateItemDto> updateRequest2 = new HttpEntity<>(updateDto2, headers);
        ResponseEntity<String> response2 = restTemplate.exchange(baseUrl + "/" + num, HttpMethod.PUT, updateRequest2, String.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).contains("업데이트2");
        assertThat(response2.getBody()).contains(ItemType.FESTIVAL.toString());
        assertThat(response2.getBody()).doesNotContain("테스트2");
        assertThat(response2.getBody()).doesNotContain(ItemType.SPORTS.toString());
    }

    @Test
    @DisplayName("DELETE - 아이템 삭제 성공")
    void DeleteItem_Success() {



        CreateItemDto createItemDto1 = new CreateItemDto(
                "테스트1", ItemType.SPECIAL,10);
        CreateItemDto createItemDto2 = new CreateItemDto(
                "테스트2", ItemType.SPORTS,10);
        CreateItemDto createItemDto3 = new CreateItemDto(
                "테스트3", ItemType.SPORTS,10);

        List<Item> itemList1 = itemRepository.findAll();


        HttpEntity<CreateItemDto> request1 = new HttpEntity<>(createItemDto1, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request1, String.class);
        List<Item> itemList = itemRepository.findAll();
        int num = 0;
        if (itemList.size() != 0) {
            num = itemList.getLast().getId();
        }
        System.out.println("num = "+num);
        HttpEntity<CreateItemDto> request2 = new HttpEntity<>(createItemDto2, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request2, String.class);


        HttpEntity<CreateItemDto> request3 = new HttpEntity<>(createItemDto3, headers);
        restTemplate.exchange(baseUrl, HttpMethod.POST, request3, String.class);


        ResponseEntity<String> response1 = restTemplate.getForEntity(baseUrl + "/" + num, String.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response1.getBody()).contains("테스트1");
        assertThat(response1.getBody()).contains(ItemType.SPECIAL.toString());


        restTemplate.delete(baseUrl + "/" + num);
        ResponseEntity<String> response11 = restTemplate.getForEntity(baseUrl + "/" + num, String.class);
        assertThat(response11.getBody()).contains("404");
        assertThat(response11.getBody()).contains("아이템이 존재하지 않습니다.");
    }

    private static final Logger log = LoggerFactory.getLogger(ApiV1ItemControllerTest.class);

}