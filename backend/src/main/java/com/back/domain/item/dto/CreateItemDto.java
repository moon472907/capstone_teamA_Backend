package com.back.domain.item.dto;

import com.back.domain.item.entity.Item;
import com.back.domain.item.entity.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateItemDto(
        @NotBlank String name,
        @NotNull ItemType itemType,
        @NotNull int price) {

    public CreateItemDto(String name, ItemType itemType, int price) {

        this.name = name;
        this.itemType = itemType;
        this.price = price;
    }

    public CreateItemDto(Item item) {

        this(
                item.getName(),
                item.getType(),
                item.getPrice()
        );
    }

}
