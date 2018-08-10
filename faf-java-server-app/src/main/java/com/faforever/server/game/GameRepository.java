package com.faforever.server.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface GameRepository extends JpaRepository<Game, Integer> {

  @Query("select max(g.id) from Game g")
  Optional<Integer> findMaxId();

  /** Updates the status of all games that have no {@code endTime} and no {@code validity} specified. */
  @Query("update Game set validity = :validity where endTime is null and validity is null")
  @Modifying
  void updateUnfinishedGamesValidity(@Param("validity") Validity validity);
}
