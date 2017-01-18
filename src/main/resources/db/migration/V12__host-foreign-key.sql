ALTER TABLE game_stats
  ADD CONSTRAINT fk_game_stats_host FOREIGN KEY (host) REFERENCES login (id);
