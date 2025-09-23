package com.back.domain.item.dto;

import com.back.domain.item.entity.Item;
import com.back.domain.item.entity.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateItemDto (
        @NotBlank String name,
        @NotNull ItemType itemType   )
{

    public CreateItemDto(String name, ItemType itemType) {
        this.name = name;
        this.itemType = itemType;
    }
    public CreateItemDto(Item item)
    {
        this(
                item.getName(),
                item.getType()
        );
    }
}
