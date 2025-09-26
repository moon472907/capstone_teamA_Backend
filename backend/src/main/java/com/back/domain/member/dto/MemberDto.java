package com.back.domain.member.dto;

import com.back.domain.item.entity.Item;
import com.back.domain.item.entity.ItemType;
import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.MemberGender;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

public record MemberDto(
        Integer id,
        String name,
        String code,
        LocalDate birth,
        MemberGender gender,
        Integer level,
        Integer xp,
        Integer money,
        Integer title,
        Map<ItemType, Integer> items
) {
    public MemberDto(Member member) {
        this(
                member.getId(),
                member.getName(),
                member.getCode(),
                member.getBirth(),
                member.getGender(),
                member.getLevel(),
                member.getXp(),
                member.getMoney(),
                member.getTitle() != null ? member.getTitle().getId() : null,
                buildItems(member)
        );
    }

    private static Map<ItemType, Integer> buildItems(Member member) {
        Map<ItemType, Integer> map = new EnumMap<>(ItemType.class);
        for (ItemType type : ItemType.values()) {
            Item item = member.getItems().get(type);
            map.put(type, item != null ? item.getId() : null);
        }
        return map;
    }
}