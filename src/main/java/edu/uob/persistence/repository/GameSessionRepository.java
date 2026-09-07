package edu.uob.persistence.repository;

import edu.uob.persistence.entity.GameSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSessionEntity, String> {
    Optional<GameSessionEntity> findBySaveSlotName(String saveSlotName);
}
