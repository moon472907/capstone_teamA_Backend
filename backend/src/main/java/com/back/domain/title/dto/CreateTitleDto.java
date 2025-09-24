package com.back.domain.title.dto;

public record CreateTitleDto(
        String content
) {
    public CreateTitleDto(String content)
    {
        this.content = content;
    }
}
