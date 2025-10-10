package com.back.domain.title.dto;

import com.back.domain.title.entity.Title;

public record TitleDto(
        int id,
        String contents,
        String achiveRequire,
        String caption
) {
    public TitleDto(Title title)
    {
        this(
            title.getId(),
            title.getContent(),
            title.getAchieveRequire(),
                title.getCaption()

        );
    }
}
