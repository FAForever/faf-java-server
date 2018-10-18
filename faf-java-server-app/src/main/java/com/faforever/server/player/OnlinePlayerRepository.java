package com.faforever.server.player;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface OnlinePlayerRepository extends CrudRepository<Player, Integer> {
  Optional<Player> findByLogin(String login);

  List<Player> findAllByCountry(String country);
}
