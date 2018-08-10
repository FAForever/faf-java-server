package com.faforever.server.game;

import org.springframework.data.repository.CrudRepository;

/** Provides access to games that are currently active. */
public interface ActiveGameRepository extends CrudRepository<Game, Integer> {
}
