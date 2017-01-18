ALTER TABLE game_player_stats ADD CONSTRAINT fk_game_player_stats_player FOREIGN KEY (playerId) REFERENCES login(id);
