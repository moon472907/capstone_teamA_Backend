package com.back.domain.world.service;

import com.back.domain.world.entity.World;
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
    public List<World> findAll() {
        return worldRepository.findAll();
    }

    @Transactional(readOnly = true)
    public World findById(Integer id) {
        return worldRepository.findByIdWithNodes(id)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
    }
}
