package com.back.domain.item.controller;


import com.back.domain.item.dto.ItemDto;
import com.back.domain.item.entity.ItemType;
import com.back.domain.item.service.ItemService;
import com.back.domain.member.service.MemberService;
import com.back.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/item")
@RequiredArgsConstructor
@Tag(name = "ApiV1ItemController", description = "API 아이템 컨트롤러")
public class ApiV1ItemController {

    private final ItemService itemService;
    private final MemberService memberService;

    @PostMapping
    @Transactional
    @Operation(summary = "테스트용 아이템 생성 ")
    public ApiResponse<ItemDto> CreateItem(@RequestBody ItemDto itemDto)
    {
        ItemDto data = itemService.createItem(new ItemDto(
                itemDto.name(),
                itemDto.img(),
                itemDto.itemType()
        ));
        return new ApiResponse<>("200", "아이템 생성 성공", data);
    }


    @GetMapping
    @Transactional
    @Operation(summary = "아이템 전체 조회")
    public ApiResponse<List<ItemDto>> findAllItems()
    {
        List<ItemDto> data =itemService.ReadAllItem();
        if (data == null || data.isEmpty()) {
            return new ApiResponse<>("404", "아이템이 존재하지 않습니다.", null);
        }
        else {
            return new ApiResponse<>("200", "아이템 전체 조회 성공", data);
        }
    }

    @GetMapping("/{id}")
    @Transactional
    @Operation(summary = "아이템 단건  조회")
    public ApiResponse<ItemDto> findItemById(@PathVariable int id)
    {
        ItemDto data = itemService.ReadItemById(id);
        if (data == null) {
            return new ApiResponse<>("404", "아이템이 존재하지 않습니다.", null);
        }
        else {
            return new ApiResponse<>("200", "아이템 단건 조회 성공", data);
        }
    }

    @GetMapping("/ItemType/{category}")
    @Transactional
    @Operation(summary = "아이템 종류별 조회")
    public ApiResponse<List<ItemDto>> findAllItems(@PathVariable ItemType category)
    {
        List<ItemDto> data =itemService.ReadItemByItemType(category);
        if (data == null || data.isEmpty()) {
            return new ApiResponse<>("404", "아이템이 존재하지 않습니다.", null);
        }
        else {
            return new ApiResponse<>("200", "아이템 전체 조회 성공", data);
        }
    }


    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "아이템 수정")
    public ApiResponse<ItemDto> UpdateItem(@PathVariable int id, @RequestBody ItemDto itemDto)
    {
        ItemDto data = itemService.UpdateItem(id, itemDto);
        if (data == null) {
            return new ApiResponse<>("404", "아이템이 존재하지 않습니다.", null);
        }
        else{
            return new ApiResponse<>("200", "아이템 수정 성공", data);
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "아이템 삭제")
    public ApiResponse<ItemDto> DeleteItem(@PathVariable int id)
    {
        itemService.DeleteItem(id);
        return new ApiResponse<>("200", "아이템 삭제 성공", null);
    }

//    @PostMapping()
//    @Transactional
//    @Operation(summary = "아이템 생성")
//    public ApiResponse<ItemDto> CreateItem(@RequestBody CreateItemDto createItemDto, @RequestParam("file") MultipartFile file)
//    {
//        String imgUrl = "/app/images/"+createItemDto.name()+".jpg";
//        try{
//            file.transferTo(new File(imgUrl));
//            ItemDto data = itemService.createItem(new ItemDto(
//                    createItemDto.name(),
//                    imgUrl,
//                    createItemDto.itemType()
//            ));
//            return new ApiResponse<>("200", "아이템 생성 성공", data);
//        }
//        catch(IOException o)
//        {
//            return new ApiResponse<>("500", "이미지 저장 실패", null);
//        }
//
//    }

}

