/*
 * Splits table_mod into mod and mod_version
 */

CREATE TABLE `mod` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `display_name` varchar(100) NOT NULL UNIQUE,
  `author` varchar(100) NOT NULL,
  `uploader` mediumint(8) unsigned,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`uploader`) REFERENCES `login` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4466 DEFAULT CHARSET=latin1;

CREATE TABLE `mod_version` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `uid` varchar(40) NOT NULL UNIQUE,
  `type` ENUM('UI', 'SIM') NOT NULL COMMENT 'This field can be defined for every version because maybe the author finds a way to turn his SIM mod into an UI mod.',
  `description` longtext NOT NULL,
  `version` smallint(5) NOT NULL,
  `filename` varchar(255) NOT NULL UNIQUE,
  `icon` varchar(255),
  `ranked` tinyint(1) NOT NULL DEFAULT '0',
  `hidden` tinyint(1) NOT NULL DEFAULT '0',
  `mod_id` mediumint(8) unsigned NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `mod_id_version` (`mod_id`,`version`),
  FOREIGN KEY (`mod_id`) REFERENCES `mod` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6446 DEFAULT CHARSET=latin1;

CREATE TABLE `mod_stats` (
  `mod_id` mediumint(8) unsigned NOT NULL,
  `likes` float NOT NULL DEFAULT '0',
  `likers` longblob NOT NULL,
  `downloads` mediumint(8) NOT NULL DEFAULT '0',
  `times_played` mediumint(8) NOT NULL DEFAULT '0',
  PRIMARY KEY (`mod_id`),
  CONSTRAINT `mod_stats_ibfk_1` FOREIGN KEY (`mod_id`) REFERENCES `mod` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


RENAME TABLE table_mod TO table_mod_old;
CREATE VIEW table_mod AS (select
        v.id,
        v.uid,
        m.display_name as name,
        v.version,
        author,
        CASE v.type WHEN 'UI' THEN 1 WHEN 'SIM' THEN 0 END as ui,
        v.create_time as `date`,
        COALESCE(s.downloads, 0) as downloads,
        COALESCE(s.likes, 0) as likes,
        COALESCE(s.times_played, 0) as played,
        v.description,
        s.likers as likers,
        v.filename,
        v.icon,
        v.ranked
    from `mod` m
    join mod_version v on m.id = v.mod_id
    left join mod_stats s on m.id = s.mod_id);

START TRANSACTION;

insert into `mod` (display_name, author, uploader)
    select name, author, (select id from login where login = author)
        from table_mod_old m1
        where m1.id = (select MAX(id) from table_mod_old m2 where m2.name = m1.name);

insert into mod_version (uid, type, description, version, filename, icon, ranked, hidden, mod_id, create_time)
    select
        uid,
        CASE ui WHEN 0 THEN 'SIM' WHEN 1 THEN 'UI' END,
        description,
        version,
        filename,
        icon,
        ranked,
        0,
        m.id,
        `date`
    from table_mod_old o
    join `mod` m on m.display_name = o.name;


SET @saved_group_concat_max_len = @@group_concat_max_len;
SET group_concat_max_len = 10240;

insert into mod_stats (mod_id, likes, downloads, times_played, likers)
    select
        m.id, sum(likes), sum(downloads), sum(played), COALESCE(CONCAT('[', GROUP_CONCAT(likers_table.likers SEPARATOR ', '), ']'), '')
    from table_mod_old o
    join `mod` m on m.display_name = o.name
    join (select name, REPLACE(REPLACE(IF(likers = '', NULL, IF(likers = '[]', NULL, likers)), '[', ''), ']', '') as likers from table_mod_old) as likers_table
        on likers_table.name = o.name
    group by o.name;

SET group_concat_max_len = @saved_group_concat_max_len;

COMMIT;

drop table table_mod_old;
