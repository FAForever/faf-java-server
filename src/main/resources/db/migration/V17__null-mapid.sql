ALTER TABLE game_stats MODIFY mapId MEDIUMINT(8) UNSIGNED;

UPDATE game_stats SET mapId = NULL WHERE mapId = 0;
