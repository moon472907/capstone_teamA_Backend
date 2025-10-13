package com.back.domain.item.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Item extends BaseEntity {
    private ItemType type;
    private String name;
    private String img;
    private int price;

    public Item(ItemType type, String name, String img, int price)
    {
        this.type = type;
        this.name = name;
        this.img = img;
        this.price = price;
    }

}
