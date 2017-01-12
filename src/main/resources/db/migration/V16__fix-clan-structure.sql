# We drop the old table to have a clean start
DROP TABLE `clan_members`;
DROP TABLE `clan_list`;

CREATE TABLE `clan` (
  `id` mediumint(8) UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(40) NOT NULL,
  `tag` varchar(3) NOT NULL,
  `founder_id` mediumint(8) UNSIGNED DEFAULT NULL COMMENT 'The initial creator of the clan',
  `leader_id` mediumint(8) UNSIGNED DEFAULT NULL COMMENT 'Current leader/admin of the clan',
  `description` text,
  `tag_color` varchar(6) DEFAULT NULL COMMENT 'RGB color code for the clan''s tag in chat',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`founder_id`) REFERENCES `login`(`id`),
  FOREIGN KEY (`leader_id`) REFERENCES `login`(`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=latin1;

CREATE TABLE `clan_membership` (
  `id` mediumint(8) UNSIGNED NOT NULL AUTO_INCREMENT,
  `clan_id` mediumint(8) UNSIGNED NOT NULL,
  `player_id` mediumint(8) UNSIGNED NOT NULL UNIQUE,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`clan_id`) REFERENCES `clan`(`id`),
  FOREIGN KEY (`player_id`) REFERENCES `login`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


