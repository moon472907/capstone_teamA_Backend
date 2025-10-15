package com.back.domain.party.party.dto;

import com.back.domain.item.entity.Item;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ItemDecorationDto {
    private Integer id;
    private String name;
    private String iconUrl; // Item.img 필드를 iconUrl로 매핑

    public ItemDecorationDto(Item item) {
        if (item != null) {
            this.id = item.getId();
            this.name = item.getName();
            // Item Entity의 img 필드가 아이콘 URL 경로를 담고 있다고 가정
            this.iconUrl = item.getImg();
        }
    }
}