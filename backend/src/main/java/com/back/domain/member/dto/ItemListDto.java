package com.back.domain.member.dto;

import com.back.domain.item.entity.Item;
import com.back.domain.item.entity.ItemType;

import java.util.*;
import java.util.stream.Collectors;

public record ItemListDto(
        Map<ItemType, List<Integer>> items
) {
    public ItemListDto(Set<Item> ownedItems) {
        this(
                EnumSet.allOf(ItemType.class).stream()
                        .collect(Collectors.toMap(
                                type -> type,
                                type -> new ArrayList<>()
                        ))
        );
        ownedItems.forEach(item -> this.items.get(item.getType()).add(item.getId()));
        this.items.replaceAll((k, v) -> v.stream().sorted().toList());
    }
}