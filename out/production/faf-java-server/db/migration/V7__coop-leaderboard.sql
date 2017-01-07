-- Null is allowed because
--   a) some of the games are in game_player_stats_bak
--   b) some existing, new entries have no corresponding game_player_stats for unknown reasons
ALTER TABLE coop_leaderboard ADD COLUMN player_count TINYINT(2);

UPDATE coop_leaderboard
SET
  player_count = (SELECT COUNT(*)
                  FROM game_player_stats
                    WHERE gameid = gameuid
                  GROUP BY gameid);
