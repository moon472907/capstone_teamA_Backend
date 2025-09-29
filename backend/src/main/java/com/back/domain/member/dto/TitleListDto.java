package com.back.domain.member.dto;

import com.back.domain.title.entity.Title;

import java.util.List;
import java.util.Set;

public record TitleListDto(
        List<Integer> titles
) {
        public TitleListDto(Set<Title> ownedTitles) {
            this(
                    ownedTitles.stream()
                            .map(Title::getId)
                            .sorted()
                            .toList()
            );
        }
}