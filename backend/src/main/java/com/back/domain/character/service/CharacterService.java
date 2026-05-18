package com.back.domain.character.service;

import com.back.domain.character.dto.CharacterDto;
import com.back.domain.character.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;

    @Transactional(readOnly = true)
    public List<CharacterDto> getAll() {
        return characterRepository.findAll()
                .stream()
                .map(CharacterDto::from)
                .toList();
    }
}
