package com.back.domain.title.dto;

import com.back.domain.title.entity.Title;

public record TitleDto(
        int id,
        String contents
) {
    public TitleDto(Title title)
    {
        this(
            title.getId(),
            title.getContent()
        );
    }
}
