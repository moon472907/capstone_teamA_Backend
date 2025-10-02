package com.back.domain.title.dto;

public record CreateTitleDto(
        String content,
        String achiveRequire
) {
    public CreateTitleDto(String content, String achiveRequire)
    {
        this.content = content;
        this.achiveRequire = achiveRequire;
    }
}
