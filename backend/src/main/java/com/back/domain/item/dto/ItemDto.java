package com.back.domain.item.dto;

import com.back.domain.item.entity.Item;
import com.back.domain.item.entity.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemDto (
        @NotBlank int id,
      @NotBlank String name,
      @NotBlank String img,
      @NotNull  ItemType itemType   )
{

    public ItemDto(int id , String name, String img, ItemType itemType) {
        this.id = id;
        this.name = name;
        this.img = img;
        this.itemType = itemType;
    }
    public ItemDto(Item item)
    {
      this(     item.getId(),
                item.getName(),
                item.getImg(),
                item.getType()
      );
    }
}
