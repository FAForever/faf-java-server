package com.faforever.server.game;

import com.faforever.server.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface GameRepository extends JpaRepository<Game, Integer> {

  @Query("select max(g.id) from Game g")
  Optional<Integer> findMaxId();
}
