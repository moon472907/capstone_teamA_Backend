package com.back.domain.character.dto;

import com.back.domain.character.entity.GameCharacter;

public record CharacterDto(
        Integer id,
        String characterKey,
        String name,
        String icon,
        String description
) {
    public static CharacterDto from(GameCharacter character) {
        return new CharacterDto(
                character.getId(),
                character.getCharacterKey(),
                character.getName(),
                character.getIcon(),
                character.getDescription()
        );
    }
}
