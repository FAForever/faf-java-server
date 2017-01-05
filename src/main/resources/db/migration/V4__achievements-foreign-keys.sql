ALTER TABLE `player_achievements` CHANGE `player_id` `player_id` mediumint(8) unsigned NOT NULL;

ALTER TABLE `player_achievements` ADD CONSTRAINT `fk_login` FOREIGN KEY (`player_id`) REFERENCES `login` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `player_achievements` ADD CONSTRAINT `fk_achievement` FOREIGN KEY (`achievement_id`) REFERENCES `achievement_definitions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
