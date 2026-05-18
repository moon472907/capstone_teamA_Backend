package com.back.domain.character.repository;

import com.back.domain.character.entity.GameCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CharacterRepository extends JpaRepository<GameCharacter, Integer> {

    Optional<GameCharacter> findByCharacterKey(String characterKey);

    boolean existsByCharacterKey(String characterKey);
}
