CREATE VIEW achievement_statistics AS
  SELECT
    achievement_id,
    COALESCE(unlock_stats.count, 0)                                           AS unlockers_count,
    COALESCE(ROUND(100 * (unlock_stats.count / achievers_count.count), 2), 0) AS unlockers_percent,
    unlock_stats.min_time                                                     AS unlockers_min_duration,
    unlock_stats.avg_time                                                     AS unlockers_avg_duration,
    unlock_stats.max_time                                                     AS unlockers_max_duration
  FROM
    achievement_definitions ach
    JOIN (SELECT
            achievement_id,
            COUNT(*)                                  AS COUNT,
            ROUND(MIN(TIMESTAMPDIFF(SECOND, GREATEST(ach.create_time, login.create_time),
                                    pa.update_time))) AS min_time,
            ROUND(AVG(TIMESTAMPDIFF(SECOND, GREATEST(ach.create_time, login.create_time),
                                    pa.update_time))) AS avg_time,
            ROUND(MAX(TIMESTAMPDIFF(SECOND, GREATEST(ach.create_time, login.create_time),
                                    pa.update_time))) AS max_time
          FROM achievement_definitions ach
            LEFT JOIN player_achievements pa
              ON pa.achievement_id = ach.id
            LEFT JOIN login
              ON login.id = pa.player_id
          WHERE pa.state = 'UNLOCKED'
          GROUP BY achievement_id
         ) AS unlock_stats
      ON unlock_stats.achievement_id = ach.id
    ,
    (SELECT COUNT(*) AS COUNT
     FROM login
     WHERE id IN (SELECT player_id
                  FROM player_achievements)) AS achievers_count;
