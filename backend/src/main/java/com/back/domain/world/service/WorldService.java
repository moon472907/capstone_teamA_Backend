package com.back.domain.world.service;

import com.back.domain.world.dto.WorldDto;
import com.back.domain.world.repository.WorldRepository;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorldService {

    private final WorldRepository worldRepository;

    @Transactional(readOnly = true)
    public List<WorldDto> findAll() {
        return worldRepository.findAll().stream()
                .map(WorldDto::summary)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorldDto findById(Integer id) {
        return worldRepository.findByIdWithNodes(id)
                .map(WorldDto::full)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
    }
}
