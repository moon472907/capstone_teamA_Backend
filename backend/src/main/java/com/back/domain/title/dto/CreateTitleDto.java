package com.back.domain.title.dto;

public record CreateTitleDto(
        String content,
        String achiveRequire,
        String caption
) {
    public CreateTitleDto(String content, String achiveRequire,      String caption)
    {
        this.content = content;
        this.achiveRequire = achiveRequire;
        this.caption = caption;
    }
}
