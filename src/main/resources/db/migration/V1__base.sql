-- DON'T EVEN THINK OF UPDATING THIS FILE
-- DON'T EVEN THINK OF UPDATING THIS FILE
-- DON'T EVEN THINK OF UPDATING THIS FILE
-- DON'T EVEN THINK OF UPDATING THIS FILE
-- DON'T EVEN THINK OF UPDATING THIS FILE
-- DON'T EVEN THINK OF UPDATING THIS FILE
-- DON'T EVEN THINK OF UPDATING THIS FILE
-- DON'T EVEN THINK OF UPDATING THIS FILE
-- DON'T EVEN THINK OF UPDATING THIS FILE
-- DON'T EVEN THINK OF UPDATING THIS FILE
-- DON'T EVEN THINK OF UPDATING THIS FILE
-- DON'T EVEN THINK OF UPDATING THIS FILE
-- DON'T EVEN THINK OF UPDATING THIS FILE
-- DON'T EVEN THINK OF UPDATING THIS FILE
-- DON'T EVEN THINK OF UPDATING THIS FILE
-- DON'T EVEN THINK OF UPDATING THIS FILE
-- DON'T EVEN THINK OF UPDATING THIS FILE
-- DON'T EVEN THINK OF UPDATING THIS FILE
-- DON'T EVEN THINK OF UPDATING THIS FILE
-- DON'T EVEN THINK OF UPDATING THIS FILE

-- This is a dump of the production database and it is and will be our base version.
-- From this point on forward, we will ONLY ADD MIGRATION SCRIPTS, never ever modify this or any other migration script.
-- If someone needs a new database, the way to go is to apply all migration scripts in sequence using flyway.
-- Only then we can be sure that two databases of the same version actually look the same.










-- MySQL dump 10.13  Distrib 5.7.14, for Linux (x86_64)
--
-- Host: localhost    Database: faf_lobby
-- ------------------------------------------------------
-- Server version	5.7.10

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `AI_names`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AI_names` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary Key : ID .\nUnsigned car pas de valeurs negative.\nMEDIUMINT : 16 millions d''entrÃ©es maxi, meilleures perfs que INT\nSMALLINT : 65.000 entrÃ©es, ce qui peut etre largement suffisant.\nDoit etre contraint a l''id de la table info_clients.',
  `login` varchar(60) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL COMMENT 'login du clientMinimum 5 caracteres, VARCHAR peut fragmenter la DB, mais pas d''autres moyens ici.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_login` (`login`)
) ENGINE=InnoDB AUTO_INCREMENT=16777215 DEFAULT CHARSET=latin1 COMMENT='login';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AI_rating`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AI_rating` (
  `id` mediumint(8) unsigned NOT NULL,
  `mean` float DEFAULT NULL,
  `deviation` float DEFAULT NULL,
  `numGames` smallint(4) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  CONSTRAINT `AI_rating_ibfk_1` FOREIGN KEY (`id`) REFERENCES `AI_names` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `achievement_definitions`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `achievement_definitions` (
  `id` varchar(36) NOT NULL COMMENT 'The ID of the achievement.',
  `order` int(10) unsigned NOT NULL COMMENT 'The order in which the achievement is displayed to the user.',
  `name_key` varchar(255) NOT NULL COMMENT 'The message key for the name of the achievement.',
  `description_key` varchar(255) NOT NULL COMMENT 'The message key for the description of the achievement.',
  `type` enum('STANDARD','INCREMENTAL') NOT NULL COMMENT 'The type of the achievement. \nPossible values are:\n"STANDARD" - Achievement is either locked or unlocked.\n"INCREMENTAL" - Achievement is incremental.',
  `total_steps` int(10) unsigned DEFAULT NULL COMMENT 'The total steps for an incremental achievement, NULL for standard achievements.',
  `revealed_icon_url` varchar(2000) DEFAULT NULL COMMENT 'The image URL for the revealed achievement icon.',
  `unlocked_icon_url` varchar(2000) DEFAULT NULL COMMENT 'The image URL for the unlocked achievement icon.',
  `initial_state` enum('HIDDEN','REVEALED') NOT NULL COMMENT 'The initial state of the achievement. \nPossible values are:\n"HIDDEN" - Achievement is hidden.\n"REVEALED" - Achievement is revealed.\n"UNLOCKED" - Achievement is unlocked.',
  `experience_points` int(10) unsigned NOT NULL COMMENT 'Experience points which will be earned when unlocking this achievement. Multiple of 5. Reference:\n5 - Easy to achieve\n20 - Medium\n50 - Hard to achieve',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `name_key_UNIQUE` (`name_key`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `auth_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(80) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `auth_group_permissions`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_group_permissions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id` (`group_id`,`permission_id`),
  KEY `auth_group_permissions_bda51c3c` (`group_id`),
  KEY `auth_group_permissions_1e014c8f` (`permission_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `auth_permission`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_permission` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `content_type_id` int(11) NOT NULL,
  `codename` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `content_type_id` (`content_type_id`,`codename`),
  KEY `auth_permission_e4470c6e` (`content_type_id`)
) ENGINE=MyISAM AUTO_INCREMENT=19 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `auth_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(30) NOT NULL,
  `first_name` varchar(30) NOT NULL,
  `last_name` varchar(30) NOT NULL,
  `email` varchar(75) NOT NULL,
  `password` varchar(128) NOT NULL,
  `is_staff` tinyint(1) NOT NULL,
  `is_active` tinyint(1) NOT NULL,
  `is_superuser` tinyint(1) NOT NULL,
  `last_login` datetime NOT NULL,
  `date_joined` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `auth_user_groups`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_user_groups` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `group_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`,`group_id`),
  KEY `auth_user_groups_fbfc09f1` (`user_id`),
  KEY `auth_user_groups_bda51c3c` (`group_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `auth_user_user_permissions`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_user_user_permissions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`,`permission_id`),
  KEY `auth_user_user_permissions_fbfc09f1` (`user_id`),
  KEY `auth_user_user_permissions_1e014c8f` (`permission_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `avatars`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `avatars` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `idUser` mediumint(8) unsigned NOT NULL,
  `idAvatar` int(10) unsigned NOT NULL,
  `selected` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idUser` (`idUser`,`idAvatar`),
  KEY `friendCnst` (`idAvatar`),
  CONSTRAINT `avatars_ibfk_1` FOREIGN KEY (`idUser`) REFERENCES `login` (`id`),
  CONSTRAINT `avatars_ibfk_2` FOREIGN KEY (`idAvatar`) REFERENCES `avatars_list` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2483 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `avatars_list`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `avatars_list` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `url` varchar(255) NOT NULL,
  `tooltip` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `url` (`url`,`tooltip`)
) ENGINE=InnoDB AUTO_INCREMENT=254 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `avatars_list_copy_812015`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `avatars_list_copy_812015` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `url` varchar(255) NOT NULL,
  `tooltip` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `url` (`url`,`tooltip`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bet`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bet` (
  `userid` mediumint(8) unsigned NOT NULL,
  `amount` int(11) NOT NULL,
  PRIMARY KEY (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bugreport_status`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bugreport_status` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bugreport` int(11) NOT NULL,
  `status_code` enum('unfiled','filed','dismissed') NOT NULL,
  `url` varchar(255) DEFAULT NULL COMMENT 'If status is filed, then this should be a reference to a github issue',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When this entry was created.',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When this entry was created.',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `bugreport` (`bugreport`),
  CONSTRAINT `bugreport` FOREIGN KEY (`bugreport`) REFERENCES `bugreports` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bugreport_targets`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bugreport_targets` (
  `id` varchar(255) NOT NULL COMMENT 'Unique reference to the target, e.g. FAForever/client/tree/(hash)',
  `name` varchar(255) NOT NULL COMMENT 'Name of the target, a github repository name',
  `ref` varchar(255) NOT NULL COMMENT 'Reference of the target',
  `url` varchar(255) NOT NULL COMMENT 'Url to the target',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bugreports`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bugreports` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `target` varchar(255) NOT NULL,
  `automatic` tinyint(1) NOT NULL COMMENT 'Whether the report was automated or not',
  `description` text COMMENT 'A (potentially markdown-formatted) description of the bug',
  `log` text COMMENT 'Log associated with the report',
  `traceback` text COMMENT 'Traceback associated with the report',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `bugreport_target` (`target`),
  CONSTRAINT `bugreport_target` FOREIGN KEY (`target`) REFERENCES `bugreport_targets` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `clan_list`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `clan_list` (
  `clan_id` int(11) NOT NULL AUTO_INCREMENT,
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` int(1) NOT NULL DEFAULT '0',
  `clan_name` varchar(40) NOT NULL,
  `clan_tag` varchar(3) DEFAULT NULL,
  `clan_founder_id` mediumint(8) DEFAULT NULL,
  `clan_leader_id` mediumint(8) DEFAULT NULL,
  `clan_desc` text,
  PRIMARY KEY (`clan_id`)
) ENGINE=InnoDB AUTO_INCREMENT=823 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `clan_members`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `clan_members` (
  `clan_id` int(11) NOT NULL,
  `player_id` int(11) NOT NULL,
  `join_clan_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`clan_id`,`player_id`),
  KEY `player_id` (`player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `coop_leaderboard`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `coop_leaderboard` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `mission` smallint(6) unsigned NOT NULL,
  `gameuid` bigint(20) unsigned NOT NULL,
  `secondary` tinyint(3) unsigned NOT NULL,
  `time` time NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `gameuid` (`gameuid`)
) ENGINE=InnoDB AUTO_INCREMENT=29189 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `coop_map`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `coop_map` (
  `type` tinyint(3) unsigned NOT NULL,
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(40) DEFAULT NULL,
  `description` longtext,
  `version` decimal(4,0) DEFAULT NULL,
  `filename` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `filename` (`filename`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `db_version`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `db_version` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `script` varchar(255) DEFAULT NULL,
  `revision` varchar(40) NOT NULL COMMENT 'The revision of the database after the script execution',
  `execution_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When this script was executed.',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `email_domain_blacklist`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `email_domain_blacklist` (
  `domain` varchar(255) NOT NULL,
  UNIQUE KEY `domain_index` (`domain`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `event_definitions`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event_definitions` (
  `id` varchar(36) NOT NULL COMMENT 'The ID of the event.',
  `name_key` varchar(255) NOT NULL COMMENT 'The message key for the name of the event.',
  `image_url` varchar(45) DEFAULT NULL COMMENT 'The base URL for the image that represents the event.',
  `type` enum('NUMERIC','TIME') NOT NULL COMMENT 'The type of the event.\nPossible values are:\n"NUMERIC" - Event is a plain number.\n"TIME" - Event is a measure of time.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `name_key_UNIQUE` (`name_key`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `faction`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `faction` (
  `id` tinyint(3) unsigned DEFAULT NULL,
  `name` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `featured_mods_owners`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `featured_mods_owners` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `uid` mediumint(8) unsigned NOT NULL,
  `moduid` tinyint(3) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `uid` (`uid`),
  KEY `moduid` (`moduid`),
  CONSTRAINT `featured_mods_owners_ibfk_1` FOREIGN KEY (`moduid`) REFERENCES `game_featuredMods` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `featured_mods_owners_ibfk_2` FOREIGN KEY (`uid`) REFERENCES `login` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `friends_and_foes`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `friends_and_foes` (
  `user_id` mediumint(8) unsigned NOT NULL,
  `subject_id` mediumint(8) unsigned NOT NULL,
  `status` enum('FRIEND','FOE') DEFAULT NULL,
  PRIMARY KEY (`user_id`,`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_featuredMods`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game_featuredMods` (
  `id` tinyint(3) unsigned NOT NULL AUTO_INCREMENT,
  `gamemod` varchar(50) DEFAULT NULL,
  `description` text NOT NULL,
  `name` varchar(255) NOT NULL,
  `publish` tinyint(1) NOT NULL DEFAULT '0',
  `order` smallint(4) unsigned NOT NULL DEFAULT '0' COMMENT 'Order in the featured mods list',
  PRIMARY KEY (`id`),
  UNIQUE KEY `mod_name_idx` (`gamemod`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_min_rating`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game_min_rating` (
  `id` bigint(20) unsigned NOT NULL,
  `minRating` float DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `game_min_rating_ibfk_1` FOREIGN KEY (`id`) REFERENCES `game_stats_bak` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_player_stats`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game_player_stats` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `gameId` bigint(20) unsigned NOT NULL,
  `playerId` mediumint(8) unsigned NOT NULL,
  `AI` tinyint(1) NOT NULL,
  `faction` tinyint(3) unsigned NOT NULL,
  `color` tinyint(4) NOT NULL,
  `team` tinyint(3) NOT NULL,
  `place` tinyint(3) unsigned NOT NULL,
  `mean` float unsigned NOT NULL,
  `deviation` float unsigned NOT NULL,
  `after_mean` float DEFAULT NULL,
  `after_deviation` float DEFAULT NULL,
  `score` tinyint(3) NOT NULL,
  `scoreTime` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `playerId` (`playerId`),
  KEY `gameIdIdx` (`gameId`)
) ENGINE=InnoDB AUTO_INCREMENT=9609268 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_player_stats_bak`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game_player_stats_bak` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `gameId` bigint(20) unsigned NOT NULL,
  `playerId` mediumint(8) unsigned NOT NULL,
  `AI` tinyint(1) NOT NULL,
  `faction` tinyint(3) unsigned NOT NULL,
  `color` tinyint(4) NOT NULL,
  `team` tinyint(3) NOT NULL,
  `place` tinyint(3) unsigned NOT NULL,
  `mean` float unsigned NOT NULL,
  `deviation` float unsigned NOT NULL,
  `after_mean` float DEFAULT NULL,
  `after_deviation` float DEFAULT NULL,
  `score` tinyint(3) NOT NULL,
  `scoreTime` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `playerId` (`playerId`),
  KEY `gameIdIdx` (`gameId`),
  CONSTRAINT `game_player_stats_bak_ibfk_1` FOREIGN KEY (`gameId`) REFERENCES `game_stats_bak` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `game_player_stats_bak_ibfk_2` FOREIGN KEY (`playerId`) REFERENCES `login` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3461162 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_replays`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game_replays` (
  `UID` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`UID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_replays_old`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game_replays_old` (
  `UID` bigint(20) unsigned NOT NULL,
  `file` longblob NOT NULL,
  PRIMARY KEY (`UID`),
  CONSTRAINT `game_replays_old_ibfk_1` FOREIGN KEY (`UID`) REFERENCES `game_stats_bak` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_stats`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game_stats` (
  `id` int(10) unsigned NOT NULL,
  `startTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `gameType` enum('0','1','2','3') NOT NULL,
  `gameMod` tinyint(3) unsigned NOT NULL,
  `host` mediumint(8) unsigned NOT NULL,
  `mapId` mediumint(8) unsigned NOT NULL,
  `gameName` varchar(128) NOT NULL,
  `validity` tinyint(3) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `startTime` (`startTime`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER map_play_count AFTER INSERT ON game_stats FOR EACH ROW UPDATE table_map_features set times_played = (times_played +1) WHERE map_id = NEW.mapId */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `game_stats_bak`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game_stats_bak` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `startTime` timestamp NULL DEFAULT NULL,
  `EndTime` timestamp NULL DEFAULT NULL,
  `gameType` enum('0','1','2','3') DEFAULT '0',
  `gameMod` tinyint(3) unsigned DEFAULT NULL,
  `host` mediumint(8) unsigned DEFAULT NULL,
  `mapId` mediumint(8) unsigned DEFAULT NULL COMMENT 'map id',
  `gameName` tinytext,
  `valid` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `gameMod` (`gameMod`),
  KEY `host` (`host`),
  KEY `mapId` (`mapId`),
  KEY `startTime` (`startTime`),
  KEY `endTime` (`EndTime`),
  CONSTRAINT `game_stats_bak_ibfk_1` FOREIGN KEY (`mapId`) REFERENCES `map_version` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `game_stats_bak_ibfk_4` FOREIGN KEY (`gameMod`) REFERENCES `game_featuredMods` (`id`) ON DELETE SET NULL ON UPDATE SET NULL,
  CONSTRAINT `game_stats_bak_ibfk_5` FOREIGN KEY (`host`) REFERENCES `login` (`id`) ON DELETE SET NULL ON UPDATE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=1996398 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER  add_new_rating AFTER UPDATE ON `game_stats_bak` FOR EACH ROW
BEGIN
INSERT INTO game_min_rating (id, minRating)
VALUES (
NEW.id,
(
SELECT MIN( mean -3 * deviation )
FROM game_player_stats
WHERE game_player_stats.gameId = NEW.id
GROUP BY game_player_stats.gameId
)
)
ON DUPLICATE KEY UPDATE `minRating` =
(
SELECT MIN( mean -3 * deviation )
FROM game_player_stats
WHERE game_player_stats.gameId = NEW.id
GROUP BY game_player_stats.gameId
);
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `global_rating`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `global_rating` (
  `id` mediumint(8) unsigned NOT NULL,
  `mean` float DEFAULT NULL,
  `deviation` float DEFAULT NULL,
  `numGames` smallint(4) unsigned NOT NULL DEFAULT '0',
  `is_active` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  CONSTRAINT `IdCnst` FOREIGN KEY (`id`) REFERENCES `login` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `id_constraint` FOREIGN KEY (`id`) REFERENCES `login` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `invalid_game_reasons`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `invalid_game_reasons` (
  `id` tinyint(4) NOT NULL AUTO_INCREMENT,
  `message` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `jwt_users`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `jwt_users` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(20) NOT NULL,
  `public_key` varchar(1000) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ladder1v1_rating`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ladder1v1_rating` (
  `id` mediumint(8) unsigned NOT NULL,
  `mean` float DEFAULT NULL,
  `deviation` float DEFAULT NULL,
  `numGames` smallint(4) unsigned NOT NULL DEFAULT '0',
  `winGames` smallint(4) unsigned NOT NULL DEFAULT '0',
  `is_active` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  CONSTRAINT `ladder1v1_rating_ibfk_1` FOREIGN KEY (`id`) REFERENCES `login` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ladder_division`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ladder_division` (
  `id` int(10) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `league` tinyint(3) unsigned NOT NULL,
  `threshold` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ladder_divisions`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ladder_divisions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=286 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ladder_map`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ladder_map` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `idmap` mediumint(8) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idmap` (`idmap`),
  CONSTRAINT `ladder_map_ibfk_1` FOREIGN KEY (`idmap`) REFERENCES `map_version` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=357 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ladder_season_1`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ladder_season_1` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `idUser` mediumint(8) unsigned NOT NULL,
  `league` tinyint(1) unsigned NOT NULL,
  `division` smallint(5) unsigned NOT NULL,
  `score` smallint(5) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idUser` (`idUser`)
) ENGINE=InnoDB AUTO_INCREMENT=3424 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ladder_season_2`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ladder_season_2` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `idUser` mediumint(8) unsigned NOT NULL,
  `league` tinyint(1) unsigned NOT NULL,
  `division` smallint(5) unsigned NOT NULL,
  `score` smallint(5) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idUser` (`idUser`)
) ENGINE=InnoDB AUTO_INCREMENT=5085 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ladder_season_3`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ladder_season_3` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `idUser` mediumint(8) unsigned NOT NULL,
  `league` tinyint(1) unsigned NOT NULL,
  `division` smallint(5) unsigned NOT NULL,
  `score` smallint(5) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idUser` (`idUser`),
  KEY `league` (`league`),
  KEY `division` (`division`)
) ENGINE=InnoDB AUTO_INCREMENT=8065 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ladder_season_3_safe`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ladder_season_3_safe` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `idUser` mediumint(8) unsigned NOT NULL,
  `league` tinyint(1) unsigned NOT NULL,
  `division` smallint(5) unsigned NOT NULL,
  `score` smallint(5) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idUser` (`idUser`),
  KEY `league` (`league`),
  KEY `division` (`division`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ladder_season_4`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ladder_season_4` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `idUser` mediumint(8) unsigned NOT NULL,
  `league` tinyint(1) unsigned NOT NULL,
  `division` smallint(5) unsigned NOT NULL,
  `score` smallint(5) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idUser` (`idUser`),
  KEY `league` (`league`),
  KEY `division` (`division`)
) ENGINE=InnoDB AUTO_INCREMENT=5958 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ladder_season_5`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ladder_season_5` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `idUser` mediumint(8) unsigned NOT NULL,
  `league` tinyint(1) unsigned NOT NULL,
  `score` float unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idUser` (`idUser`),
  KEY `league` (`league`)
) ENGINE=InnoDB AUTO_INCREMENT=34225 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lobby_admin`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lobby_admin` (
  `user_id` int(11) NOT NULL,
  `group` tinyint(4) NOT NULL COMMENT '0 - no privileges; 1 - moderator, can delete/edit comments and approve broken maps reports; 2 - admin, same as moderator plus can add global bans',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lobby_ban`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lobby_ban` (
  `idUser` mediumint(8) unsigned NOT NULL,
  `reason` varchar(255) NOT NULL,
  `expires_at` datetime NOT NULL DEFAULT '2222-07-21 00:00:00',
  UNIQUE KEY `idUser` (`idUser`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `login`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `login` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `login` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `password` char(77) NOT NULL,
  `email` varchar(254) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `ip` varchar(15) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `steamid` bigint(20) unsigned DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_login` (`login`),
  UNIQUE KEY `unique_email` (`email`),
  UNIQUE KEY `steamid` (`steamid`)
) ENGINE=InnoDB AUTO_INCREMENT=187056 DEFAULT CHARSET=latin1 COMMENT='login';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `login_with_duplicated_users`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `login_with_duplicated_users` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `login` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `password` char(77) NOT NULL,
  `salt` char(16) DEFAULT NULL,
  `email` varchar(254) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `ip` varchar(15) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `steamid` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_login` (`login`),
  UNIQUE KEY `unique_email` (`email`),
  UNIQUE KEY `steamid` (`steamid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='login';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `map`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `map` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `display_name` varchar(100) NOT NULL,
  `map_type` varchar(15) NOT NULL,
  `battle_type` varchar(15) NOT NULL,
  `author` mediumint(8) unsigned DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When this entry was created.',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'When this entry was updated',
  PRIMARY KEY (`id`),
  UNIQUE KEY `display_name` (`display_name`),
  KEY `author` (`author`),
  CONSTRAINT `author` FOREIGN KEY (`author`) REFERENCES `login` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4466 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `map_version`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `map_version` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `description` longtext,
  `max_players` decimal(2,0) NOT NULL,
  `width` decimal(4,0) NOT NULL,
  `height` decimal(4,0) NOT NULL,
  `version` decimal(4,0) NOT NULL,
  `filename` varchar(200) NOT NULL,
  `ranked` tinyint(1) NOT NULL DEFAULT '1',
  `hidden` tinyint(1) NOT NULL DEFAULT '0',
  `map_id` mediumint(8) unsigned NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When this entry was created.',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'When this entry was updated',
  PRIMARY KEY (`id`),
  UNIQUE KEY `filename` (`filename`),
  UNIQUE KEY `map_id_version` (`map_id`,`version`),
  CONSTRAINT `map` FOREIGN KEY (`map_id`) REFERENCES `map` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6446 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `matchmaker_ban`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `matchmaker_ban` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `userid` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `messages`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `messages` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID of this entry.',
  `key` varchar(255) NOT NULL COMMENT 'The message resource key that identifies this entry along with language and region.',
  `language` char(2) NOT NULL COMMENT 'The language that identifies this entry along with key and region.',
  `region` char(2) DEFAULT NULL COMMENT 'The region that identifies this entry along with key and language.',
  `value` text COMMENT 'The message value.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `key_language_region_UNIQUE` (`key`,`language`,`region`)
) ENGINE=InnoDB AUTO_INCREMENT=143 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `name_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `name_history` (
  `change_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `user_id` mediumint(8) unsigned NOT NULL,
  `previous_name` varchar(20) NOT NULL,
  PRIMARY KEY (`change_time`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `name_history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `login` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `oauth_clients`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oauth_clients` (
  `id` varchar(36) NOT NULL COMMENT 'A string that identifies the client, preferably a UUID.',
  `name` varchar(100) NOT NULL COMMENT 'Human readable client name.',
  `client_secret` varchar(55) NOT NULL COMMENT 'The client''s secret, a random string.',
  `client_type` enum('confidential','public') NOT NULL DEFAULT 'public' COMMENT 'A string represents if the client is confidential or public.',
  `redirect_uris` text NOT NULL COMMENT 'A space delimited list of redirect URIs.',
  `default_redirect_uri` varchar(2000) NOT NULL COMMENT 'One of the redirect uris.',
  `default_scope` text NOT NULL COMMENT 'A space delimited list of default scopes of the client.',
  `icon_url` varchar(2000) DEFAULT NULL COMMENT 'URL to a square image representing the client.',
  UNIQUE KEY `name_UNIQUE` (`name`),
  UNIQUE KEY `client_id_UNIQUE` (`id`),
  UNIQUE KEY `client_secret_UNIQUE` (`client_secret`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `oauth_tokens`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oauth_tokens` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto incremented, technical ID.',
  `token_type` varchar(45) NOT NULL,
  `access_token` varchar(36) NOT NULL COMMENT 'A string token (UUID).',
  `refresh_token` varchar(36) DEFAULT NULL COMMENT 'A string token (UUID).',
  `client_id` varchar(36) NOT NULL COMMENT 'ID of the client (FK).',
  `scope` text NOT NULL COMMENT 'A space delimited list of scopes.',
  `expires` timestamp NOT NULL COMMENT 'Expiration time of the token.',
  `user_id` int(10) unsigned NOT NULL COMMENT 'ID of the user (FK).',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2165 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patchs_table`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `patchs_table` (
  `idpatchs_table` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fromMd5` varchar(45) DEFAULT NULL,
  `toMd5` varchar(45) DEFAULT NULL,
  `patchFile` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`idpatchs_table`)
) ENGINE=InnoDB AUTO_INCREMENT=7752 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player_achievements`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player_achievements` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The ID of the player achievement.',
  `player_id` int(10) unsigned NOT NULL COMMENT 'The ID of the owning player (FK).',
  `achievement_id` varchar(36) NOT NULL COMMENT 'The ID of the referenced achievement (FK).',
  `current_steps` int(10) unsigned DEFAULT NULL COMMENT 'The current steps for an incremental achievement.',
  `state` enum('HIDDEN','REVEALED','UNLOCKED') NOT NULL COMMENT 'The state of the achievement. \nPossible values are:\n"HIDDEN" - Achievement is hidden.\n"REVEALED" - Achievement is revealed.\n"UNLOCKED" - Achievement is unlocked.',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `player_achievement_UNIQUE` (`player_id`,`achievement_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1421659 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player_events`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player_events` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID of this entry.',
  `player_id` int(10) unsigned NOT NULL COMMENT 'The ID of the player that triggered this event.',
  `event_id` varchar(36) NOT NULL COMMENT 'The ID of the event definition.',
  `count` int(10) unsigned NOT NULL COMMENT 'The current number of times this event has occurred.',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `event_player_UNIQUE` (`player_id`,`event_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1763549 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `recoveryemails_enc`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `recoveryemails_enc` (
  `ID` bigint(20) unsigned zerofill NOT NULL AUTO_INCREMENT,
  `UserID` mediumint(8) unsigned NOT NULL,
  `Key` varchar(32) NOT NULL,
  `expDate` datetime NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `UserID` (`UserID`),
  CONSTRAINT `recoveryemails_enc_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `login` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=51690 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `replay_vault`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `replay_vault` (
  `id` bigint(20) unsigned NOT NULL DEFAULT '0',
  `gameName` tinytext,
  `filename` varchar(200) DEFAULT NULL,
  `startTime` timestamp NULL DEFAULT NULL,
  `EndTime` timestamp NULL DEFAULT NULL,
  `gamemod` varchar(50) DEFAULT NULL,
  `playerId` mediumint(8) unsigned NOT NULL,
  `mapId` mediumint(8) unsigned DEFAULT NULL COMMENT 'map id',
  `rating` double NOT NULL DEFAULT '0',
  `gamemodid` tinyint(3) unsigned DEFAULT NULL,
  UNIQUE KEY `id_2` (`id`,`playerId`),
  KEY `id` (`id`),
  KEY `startTime` (`startTime`),
  KEY `playerId` (`playerId`),
  KEY `rating` (`rating`),
  KEY `mapId` (`mapId`),
  KEY `gamemod` (`gamemod`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `smurf_table`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `smurf_table` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `origId` mediumint(8) unsigned NOT NULL,
  `smurfId` mediumint(8) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `origId_2` (`origId`,`smurfId`),
  KEY `origId` (`origId`),
  KEY `smurfId` (`smurfId`),
  CONSTRAINT `smurf_table_ibfk_1` FOREIGN KEY (`origId`) REFERENCES `login` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `smurf_table_ibfk_2` FOREIGN KEY (`smurfId`) REFERENCES `login` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7040 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `steam_link_request`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `steam_link_request` (
  `uid` varchar(255) NOT NULL,
  `Key` varchar(255) NOT NULL,
  `expDate` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `swiss_tournaments`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `swiss_tournaments` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `host` mediumint(8) unsigned DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `minplayers` smallint(6) DEFAULT NULL,
  `maxplayers` smallint(6) DEFAULT NULL,
  `minrating` int(11) DEFAULT NULL,
  `maxrating` int(11) DEFAULT NULL,
  `tourney_date` datetime DEFAULT NULL,
  `tourney_state` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `host` (`host`),
  CONSTRAINT `swiss_tournaments_ibfk_1` FOREIGN KEY (`host`) REFERENCES `login` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `swiss_tournaments_players`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `swiss_tournaments_players` (
  `id` smallint(5) unsigned zerofill NOT NULL AUTO_INCREMENT,
  `idtourney` mediumint(8) unsigned NOT NULL,
  `iduser` mediumint(8) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idtourney` (`idtourney`,`iduser`),
  KEY `tournament` (`idtourney`),
  KEY `iduser` (`iduser`),
  CONSTRAINT `swiss_tournaments_players_ibfk_3` FOREIGN KEY (`idtourney`) REFERENCES `swiss_tournaments` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `swiss_tournaments_players_ibfk_4` FOREIGN KEY (`iduser`) REFERENCES `login` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=597 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary view structure for view `table_map`
--

SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `table_map` AS SELECT
 1 AS `id`,
 1 AS `name`,
 1 AS `description`,
 1 AS `max_players`,
 1 AS `map_type`,
 1 AS `battle_type`,
 1 AS `map_sizeX`,
 1 AS `map_sizeY`,
 1 AS `version`,
 1 AS `filename`,
 1 AS `hidden`,
 1 AS `mapuid`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `table_map_broken`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `table_map_broken` (
  `broken_id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `map_id` mediumint(8) unsigned NOT NULL,
  `description` text NOT NULL,
  `user_id` mediumint(8) unsigned DEFAULT NULL,
  `approved` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`broken_id`),
  KEY `map_id` (`map_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `table_map_broken_ibfk_1` FOREIGN KEY (`map_id`) REFERENCES `map_version` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `table_map_broken_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `login` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=717 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `table_map_comments`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `table_map_comments` (
  `comment_id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `map_id` mediumint(8) unsigned NOT NULL,
  `user_id` mediumint(8) unsigned NOT NULL,
  `comment_text` text NOT NULL,
  `comment_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_id`),
  KEY `map_id` (`map_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `table_map_comments_ibfk_1` FOREIGN KEY (`map_id`) REFERENCES `map_version` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `table_map_comments_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `login` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3118 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `table_map_features`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `table_map_features` (
  `map_id` mediumint(8) unsigned NOT NULL,
  `rating` float NOT NULL DEFAULT '0',
  `voters` text NOT NULL,
  `downloads` int(11) NOT NULL DEFAULT '0',
  `times_played` int(11) NOT NULL DEFAULT '0',
  `num_draws` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`map_id`),
  CONSTRAINT `table_map_features_ibfk_1` FOREIGN KEY (`map_id`) REFERENCES `map_version` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `table_mod`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `table_mod` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `uid` varchar(40) NOT NULL,
  `name` varchar(255) NOT NULL,
  `version` smallint(5) unsigned NOT NULL,
  `author` varchar(100) NOT NULL,
  `ui` tinyint(4) NOT NULL,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `downloads` mediumint(8) unsigned NOT NULL DEFAULT '0',
  `likes` mediumint(8) unsigned NOT NULL DEFAULT '0',
  `played` mediumint(8) unsigned NOT NULL DEFAULT '0',
  `description` varchar(255) NOT NULL,
  `filename` varchar(255) NOT NULL,
  `icon` varchar(255) NOT NULL,
  `likers` longblob NOT NULL,
  `ranked` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uid` (`uid`)
) ENGINE=InnoDB AUTO_INCREMENT=998 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `teamkills`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `teamkills` (
  `teamkiller` mediumint(8) unsigned NOT NULL COMMENT 'login of the player who performed the teamkill',
  `victim` mediumint(8) unsigned NOT NULL COMMENT 'login of the player who got teamkilled and reported the tk',
  `game_id` int(10) unsigned NOT NULL COMMENT 'game-id where teamkill was performed',
  `gametime` mediumint(8) unsigned NOT NULL COMMENT 'time of game in seconds when tk was performed',
  `reported_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `game_id` (`game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `test_game_replays`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_game_replays` (
  `UID` bigint(20) unsigned NOT NULL,
  `file` longblob NOT NULL,
  PRIMARY KEY (`UID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tmp`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tmp` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `idUser` mediumint(8) unsigned NOT NULL,
  `idFriend` mediumint(8) unsigned NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `unique_pair` (`idUser`,`idFriend`) USING BTREE,
  KEY `friendCnst` (`idFriend`)
) ENGINE=InnoDB AUTO_INCREMENT=231435 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tutorial_sections`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tutorial_sections` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `section` varchar(45) NOT NULL,
  `description` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tutorials`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tutorials` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `section` int(10) unsigned NOT NULL,
  `name` varchar(45) NOT NULL,
  `description` varchar(255) NOT NULL,
  `url` varchar(45) NOT NULL,
  `map` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sectionIdx` (`section`),
  CONSTRAINT `tutorials_ibfk_1` FOREIGN KEY (`section`) REFERENCES `tutorial_sections` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `unique_id_users`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `unique_id_users` (
  `user_id` mediumint(8) unsigned NOT NULL,
  `uniqueid_hash` char(32) NOT NULL,
  PRIMARY KEY (`user_id`,`uniqueid_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `unique_id_users_old`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `unique_id_users_old` (
  `id` mediumint(8) NOT NULL AUTO_INCREMENT,
  `user_id` mediumint(8) unsigned NOT NULL,
  `uniqueid_hash` char(32) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=196792 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `uniqueid`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `uniqueid` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `hash` char(32) DEFAULT NULL,
  `uuid` varchar(255) NOT NULL,
  `mem_SerialNumber` varchar(255) NOT NULL,
  `deviceID` varchar(255) NOT NULL,
  `manufacturer` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `processorId` varchar(255) NOT NULL,
  `SMBIOSBIOSVersion` varchar(255) NOT NULL,
  `serialNumber` varchar(255) NOT NULL,
  `volumeSerialNumber` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uid_hash_index` (`hash`)
) ENGINE=InnoDB AUTO_INCREMENT=289563 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `uniqueid_exempt`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `uniqueid_exempt` (
  `user_id` mediumint(8) unsigned DEFAULT NULL,
  `reason` varchar(255) NOT NULL,
  UNIQUE KEY `idUser` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `file` varchar(45) DEFAULT NULL,
  `md5` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=133 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_balancetesting`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_balancetesting` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_balancetesting_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_balancetesting_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  `obselete` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=1128 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_blackops`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_blackops` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_blackops_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_blackops_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  `obselete` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_civilians`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_civilians` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_civilians_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_civilians_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  `obselete` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_claustrophobia`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_claustrophobia` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_claustrophobia_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_claustrophobia_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  `obselete` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_coop`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_coop` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_coop_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_coop_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  `obselete` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=83 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_diamond`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_diamond` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_diamond_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_diamond_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  `obselete` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_engyredesign`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_engyredesign` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_engyredesign_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_engyredesign_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_faf`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_faf` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_faf_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_faf_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  `obselete` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=746 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_fafbeta`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_fafbeta` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_fafbeta_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_fafbeta_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  `obselete` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=324 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_gw`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_gw` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_gw_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_gw_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  `obselete` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_koth`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_koth` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_koth_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_koth_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  `obselete` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_labwars`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_labwars` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_labwars_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_labwars_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  `obselete` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_matchmaker`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_matchmaker` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_matchmaker_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_matchmaker_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  `obselete` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_murderparty`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_murderparty` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_murderparty_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_murderparty_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  `obselete` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_nomads`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_nomads` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_nomads_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_nomads_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  `obselete` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_phantomx`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_phantomx` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_phantomx_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_phantomx_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  `obselete` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=95 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_supremeDestruction`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_supremeDestruction` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_supremeDestruction_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_supremeDestruction_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  `obselete` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=154 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_vanilla`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_vanilla` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_vanilla_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_vanilla_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  `obselete` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_wyvern`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_wyvern` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_wyvern_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_wyvern_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_xtremewars`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_xtremewars` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `path` varchar(45) NOT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updates_xtremewars_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updates_xtremewars_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileId` smallint(5) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `md5` varchar(45) NOT NULL,
  `obselete` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fileId` (`fileId`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vault_admin`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vault_admin` (
  `user_id` int(11) NOT NULL,
  `group` tinyint(4) NOT NULL COMMENT '0 - no privileges; 1 - moderator, can delete/edit comments and approve broken maps reports; 2 - admin, same as moderator plus can delete maps from vault',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `version_lobby`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `version_lobby` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `file` varchar(100) DEFAULT NULL,
  `version` varchar(100) NOT NULL COMMENT 'Current version of the official client',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `view_global_rating`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `view_global_rating` (
  `id` mediumint(8) unsigned NOT NULL,
  `mean` float DEFAULT NULL,
  `deviation` float DEFAULT NULL,
  `numGames` smallint(4) unsigned NOT NULL DEFAULT '0',
  `is_active` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vm_exempt`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vm_exempt` (
  `idUser` mediumint(8) unsigned DEFAULT NULL,
  `reason` varchar(255) NOT NULL,
  UNIQUE KEY `idUser` (`idUser`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Final view structure for view `table_map`
--

/*!50001 DROP VIEW IF EXISTS `table_map`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = latin1_swedish_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `table_map` AS (select `v`.`id` AS `id`,`m`.`display_name` AS `name`,`v`.`description` AS `description`,`v`.`max_players` AS `max_players`,`m`.`map_type` AS `map_type`,`m`.`battle_type` AS `battle_type`,`v`.`width` AS `map_sizeX`,`v`.`height` AS `map_sizeY`,`v`.`version` AS `version`,`v`.`filename` AS `filename`,`v`.`hidden` AS `hidden`,`m`.`id` AS `mapuid` from (`map` `m` join `map_version` `v` on((`m`.`id` = `v`.`map_id`)))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-09-15 10:38:46

--
-- Dumping data for table `achievement_definitions`
--

DELETE FROM `achievement_definitions`;
INSERT INTO `achievement_definitions` (`id`, `order`, `name_key`, `description_key`, `type`, `total_steps`, `revealed_icon_url`, `unlocked_icon_url`, `initial_state`, `experience_points`) VALUES
('c6e6039f-c543-424e-ab5f-b34df1336e81', 1, 'achievement.novice.title', 'achievement.novice.description', 'INCREMENTAL', 10, 'http://content.faforever.com/achievements/c6e6039f-c543-424e-ab5f-b34df1336e81.png', 'http://content.faforever.com/achievements/c6e6039f-c543-424e-ab5f-b34df1336e81.png', 'REVEALED', 5),
('d5c759fe-a1a8-4103-888d-3ba319562867', 2, 'achievement.junior.title', 'achievement.junior.description', 'INCREMENTAL', 50, 'http://content.faforever.com/achievements/d5c759fe-a1a8-4103-888d-3ba319562867.png', 'http://content.faforever.com/achievements/d5c759fe-a1a8-4103-888d-3ba319562867.png', 'REVEALED', 10),
('6a37e2fc-1609-465e-9eca-91eeda4e63c4', 3, 'achievement.senior.title', 'achievement.senior.description', 'INCREMENTAL', 250, 'http://content.faforever.com/achievements/6a37e2fc-1609-465e-9eca-91eeda4e63c4.png', 'http://content.faforever.com/achievements/6a37e2fc-1609-465e-9eca-91eeda4e63c4.png', 'REVEALED', 20),
('bd12277a-6604-466a-9ee6-af6908573585', 4, 'achievement.veteran.title', 'achievement.veteran.description', 'INCREMENTAL', 500, 'http://content.faforever.com/achievements/bd12277a-6604-466a-9ee6-af6908573585.png', 'http://content.faforever.com/achievements/bd12277a-6604-466a-9ee6-af6908573585.png', 'REVEALED', 30),
('805f268c-88aa-4073-aa2b-ea30700f70d6', 4, 'achievement.addict.title', 'achievement.addict.description', 'INCREMENTAL', 1000, 'http://content.faforever.com/achievements/805f268c-88aa-4073-aa2b-ea30700f70d6.png', 'http://content.faforever.com/achievements/805f268c-88aa-4073-aa2b-ea30700f70d6.png', 'REVEALED', 50),
('5b7ec244-58c0-40ca-9d68-746b784f0cad', 5, 'achievement.firstSuccess.title', 'achievement.firstSuccess.description', 'STANDARD', NULL, 'http://content.faforever.com/achievements/5b7ec244-58c0-40ca-9d68-746b784f0cad.png', 'http://content.faforever.com/achievements/5b7ec244-58c0-40ca-9d68-746b784f0cad.png', 'REVEALED', 5),
('08629902-8e18-4d92-ad14-c8ecde4a8674', 12, 'achievement.hattrick.title', 'achievement.hattrick.description', 'STANDARD', NULL, 'http://content.faforever.com/achievements/08629902-8e18-4d92-ad14-c8ecde4a8674.png', 'http://content.faforever.com/achievements/08629902-8e18-4d92-ad14-c8ecde4a8674.png', 'REVEALED', 10),
('290df67c-eb01-4fe7-9e32-caae1c10442f', 13, 'achievement.thatWasClose.title', 'achievement.thatWasClose.description', 'STANDARD', NULL, 'http://content.faforever.com/achievements/290df67c-eb01-4fe7-9e32-caae1c10442f.png', 'http://content.faforever.com/achievements/290df67c-eb01-4fe7-9e32-caae1c10442f.png', 'REVEALED', 5),
('305a8d34-42fd-42f3-ba91-d9f5e437a9a6', 14, 'achievement.topScore.title', 'achievement.topScore.description', 'STANDARD', NULL, 'http://content.faforever.com/achievements/305a8d34-42fd-42f3-ba91-d9f5e437a9a6.png', 'http://content.faforever.com/achievements/305a8d34-42fd-42f3-ba91-d9f5e437a9a6.png', 'REVEALED', 10),
('d3d2c78b-d42d-4b65-99b8-a350f119f898', 15, 'achievement.unbeatable.title', 'achievement.unbeatable.description', 'INCREMENTAL', 10, 'http://content.faforever.com/achievements/d3d2c78b-d42d-4b65-99b8-a350f119f898.png', 'http://content.faforever.com/achievements/d3d2c78b-d42d-4b65-99b8-a350f119f898.png', 'REVEALED', 20),
('02081bb0-3b7a-4a36-99ef-5ae5d92d7146', 16, 'achievement.rusher.title', 'achievement.rusher.description', 'STANDARD', NULL, 'http://content.faforever.com/achievements/02081bb0-3b7a-4a36-99ef-5ae5d92d7146.png', 'http://content.faforever.com/achievements/02081bb0-3b7a-4a36-99ef-5ae5d92d7146.png', 'REVEALED', 10),
('1a3ad9e0-53eb-47d0-9404-14dbcefbed9b', 17, 'achievement.ma12Striker.title', 'achievement.ma12Striker.description', 'INCREMENTAL', 5, 'http://content.faforever.com/achievements/1a3ad9e0-53eb-47d0-9404-14dbcefbed9b.png', 'http://content.faforever.com/achievements/1a3ad9e0-53eb-47d0-9404-14dbcefbed9b.png', 'REVEALED', 5),
('326493d7-ce2c-4a43-bbc8-3e990e2685a1', 18, 'achievement.riptide.title', 'achievement.riptide.description', 'INCREMENTAL', 25, 'http://content.faforever.com/achievements/326493d7-ce2c-4a43-bbc8-3e990e2685a1.png', 'http://content.faforever.com/achievements/326493d7-ce2c-4a43-bbc8-3e990e2685a1.png', 'REVEALED', 10),
('7d6d8c55-3e2a-41d0-a97e-d35513af1ec6', 19, 'achievement.demolisher.title', 'achievement.demolisher.description', 'INCREMENTAL', 50, 'http://content.faforever.com/achievements/7d6d8c55-3e2a-41d0-a97e-d35513af1ec6.png', 'http://content.faforever.com/achievements/7d6d8c55-3e2a-41d0-a97e-d35513af1ec6.png', 'REVEALED', 20),
('d1d50fbb-7fe9-41b0-b667-4433704b8a2c', 20, 'achievement.mantis.title', 'achievement.mantis.description', 'INCREMENTAL', 5, 'http://content.faforever.com/achievements/d1d50fbb-7fe9-41b0-b667-4433704b8a2c.png', 'http://content.faforever.com/achievements/d1d50fbb-7fe9-41b0-b667-4433704b8a2c.png', 'REVEALED', 5),
('af161922-3e52-4600-9161-d850ab0fae86', 21, 'achievement.wagner.title', 'achievement.wagner.description', 'INCREMENTAL', 25, 'http://content.faforever.com/achievements/af161922-3e52-4600-9161-d850ab0fae86.png', 'http://content.faforever.com/achievements/af161922-3e52-4600-9161-d850ab0fae86.png', 'REVEALED', 10),
('ff23024e-f533-4e23-8f8f-ecc21d5283f8', 22, 'achievement.trebuchet.title', 'achievement.trebuchet.description', 'INCREMENTAL', 50, 'http://content.faforever.com/achievements/ff23024e-f533-4e23-8f8f-ecc21d5283f8.png', 'http://content.faforever.com/achievements/ff23024e-f533-4e23-8f8f-ecc21d5283f8.png', 'REVEALED', 20),
('d656ade4-e054-415a-a2e9-5f4105f7d724', 23, 'achievement.aurora.title', 'achievement.aurora.description', 'INCREMENTAL', 5, 'http://content.faforever.com/achievements/d656ade4-e054-415a-a2e9-5f4105f7d724.png', 'http://content.faforever.com/achievements/d656ade4-e054-415a-a2e9-5f4105f7d724.png', 'REVEALED', 5),
('06a39447-66a3-4160-93d5-d48337b0cbb5', 24, 'achievement.blaze.title', 'achievement.blaze.description', 'INCREMENTAL', 25, 'http://content.faforever.com/achievements/06a39447-66a3-4160-93d5-d48337b0cbb5.png', 'http://content.faforever.com/achievements/06a39447-66a3-4160-93d5-d48337b0cbb5.png', 'REVEALED', 25),
('7f993f98-dbec-41a5-9c9a-5f85edf30767', 25, 'achievement.serenity.title', 'achievement.serenity.description', 'INCREMENTAL', 50, 'http://content.faforever.com/achievements/7f993f98-dbec-41a5-9c9a-5f85edf30767.png', 'http://content.faforever.com/achievements/7f993f98-dbec-41a5-9c9a-5f85edf30767.png', 'REVEALED', 50),
('c964ac69-b146-43d0-bd7a-cd22144f9983', 26, 'achievement.thaam.title', 'achievement.thaam.description', 'INCREMENTAL', 5, 'http://content.faforever.com/achievements/c964ac69-b146-43d0-bd7a-cd22144f9983.png', 'http://content.faforever.com/achievements/c964ac69-b146-43d0-bd7a-cd22144f9983.png', 'REVEALED', 5),
('7aa7fc88-48a2-4e49-9cd7-35e2f6ce4cec', 27, 'achievement.yenzyne.title', 'achievement.yenzyne.description', 'INCREMENTAL', 25, 'http://content.faforever.com/achievements/7aa7fc88-48a2-4e49-9cd7-35e2f6ce4cec.png', 'http://content.faforever.com/achievements/7aa7fc88-48a2-4e49-9cd7-35e2f6ce4cec.png', 'REVEALED', 10),
('6acc8bc6-1fd3-4c33-97a1-85dfed6d167a', 28, 'achievement.suthanus.title', 'achievement.suthanus.description', 'INCREMENTAL', 50, 'http://content.faforever.com/achievements/6acc8bc6-1fd3-4c33-97a1-85dfed6d167a.png', 'http://content.faforever.com/achievements/6acc8bc6-1fd3-4c33-97a1-85dfed6d167a.png', 'REVEALED', 20),
('53173f4d-450c-46f0-ac59-85834cc74972', 29, 'achievement.landLubber.title', 'achievement.landLubber.description', 'INCREMENTAL', 5, 'http://content.faforever.com/achievements/53173f4d-450c-46f0-ac59-85834cc74972.png', 'http://content.faforever.com/achievements/53173f4d-450c-46f0-ac59-85834cc74972.png', 'REVEALED', 5),
('2d5cd544-4fc8-47b9-8ebb-e72ed6423d51', 30, 'achievement.seaman.title', 'achievement.seaman.description', 'INCREMENTAL', 25, 'http://content.faforever.com/achievements/2d5cd544-4fc8-47b9-8ebb-e72ed6423d51.png', 'http://content.faforever.com/achievements/2d5cd544-4fc8-47b9-8ebb-e72ed6423d51.png', 'REVEALED', 10),
('bd77964b-c06b-4649-bf7c-d35cb7715854', 31, 'achievement.admiralOfTheFleet.title', 'achievement.admiralOfTheFleet.description', 'INCREMENTAL', 50, 'http://content.faforever.com/achievements/bd77964b-c06b-4649-bf7c-d35cb7715854.png', 'http://content.faforever.com/achievements/bd77964b-c06b-4649-bf7c-d35cb7715854.png', 'REVEALED', 20),
('c1ccde26-8449-4625-b769-7d8f75fa8df3', 32, 'achievement.wrightBrother.title', 'achievement.wrightBrother.description', 'INCREMENTAL', 5, 'http://content.faforever.com/achievements/c1ccde26-8449-4625-b769-7d8f75fa8df3.png', 'http://content.faforever.com/achievements/c1ccde26-8449-4625-b769-7d8f75fa8df3.png', 'REVEALED', 5),
('a4ade3d4-d541-473f-9788-e92339446d75', 33, 'achievement.wingman.title', 'achievement.wingman.description', 'INCREMENTAL', 25, 'http://content.faforever.com/achievements/a4ade3d4-d541-473f-9788-e92339446d75.png', 'http://content.faforever.com/achievements/a4ade3d4-d541-473f-9788-e92339446d75.png', 'REVEALED', 10),
('e220d5e6-481c-4347-ac69-b6b6f956bc0f', 34, 'achievement.kingOfTheSkies.title', 'achievement.kingOfTheSkies.description', 'INCREMENTAL', 50, 'http://content.faforever.com/achievements/e220d5e6-481c-4347-ac69-b6b6f956bc0f.png', 'http://content.faforever.com/achievements/e220d5e6-481c-4347-ac69-b6b6f956bc0f.png', 'REVEALED', 20),
('e5c63aec-20a0-4263-841d-b7bc45209713', 35, 'achievement.militiaman.title', 'achievement.militiaman.description', 'INCREMENTAL', 5, 'http://content.faforever.com/achievements/e5c63aec-20a0-4263-841d-b7bc45209713.png', 'http://content.faforever.com/achievements/e5c63aec-20a0-4263-841d-b7bc45209713.png', 'REVEALED', 5),
('ec8faec7-e3e1-436e-a1ac-9f7adc3d0387', 36, 'achievement.grenadier.title', 'achievement.grenadier.description', 'INCREMENTAL', 25, 'http://content.faforever.com/achievements/ec8faec7-e3e1-436e-a1ac-9f7adc3d0387.png', 'http://content.faforever.com/achievements/ec8faec7-e3e1-436e-a1ac-9f7adc3d0387.png', 'REVEALED', 10),
('10f17c75-1154-447d-a4f7-6217add0407e', 37, 'achievement.fieldMarshal.title', 'achievement.fieldMarshal.description', 'INCREMENTAL', 50, 'http://content.faforever.com/achievements/10f17c75-1154-447d-a4f7-6217add0407e.png', 'http://content.faforever.com/achievements/10f17c75-1154-447d-a4f7-6217add0407e.png', 'REVEALED', 20),
('06b19364-5aab-4bce-883d-975f663d2091', 38, 'achievement.techie.title', 'achievement.techie.description', 'INCREMENTAL', 5, 'http://content.faforever.com/achievements/06b19364-5aab-4bce-883d-975f663d2091.png', 'http://content.faforever.com/achievements/06b19364-5aab-4bce-883d-975f663d2091.png', 'REVEALED', 5),
('cd64c5e7-b063-4543-9f52-0e87883b33a9', 39, 'achievement.iLoveBigToys.title', 'achievement.iLoveBigToys.description', 'INCREMENTAL', 25, 'http://content.faforever.com/achievements/cd64c5e7-b063-4543-9f52-0e87883b33a9.png', 'http://content.faforever.com/achievements/cd64c5e7-b063-4543-9f52-0e87883b33a9.png', 'REVEALED', 10),
('e8af7cc9-aaa6-4d0e-8e5a-481702a83a4e', 40, 'achievement.experimentalist.title', 'achievement.experimentalist.description', 'INCREMENTAL', 50, 'http://content.faforever.com/achievements/e8af7cc9-aaa6-4d0e-8e5a-481702a83a4e.png', 'http://content.faforever.com/achievements/e8af7cc9-aaa6-4d0e-8e5a-481702a83a4e.png', 'REVEALED', 20),
('045342e1-ae0d-4ef6-98bc-0bb54ffe00b3', 41, 'achievement.whatASwarm.title', 'achievement.whatASwarm.description', 'INCREMENTAL', 500, 'http://content.faforever.com/achievements/045342e1-ae0d-4ef6-98bc-0bb54ffe00b3.png', 'http://content.faforever.com/achievements/045342e1-ae0d-4ef6-98bc-0bb54ffe00b3.png', 'REVEALED', 10),
('d38aec23-e487-4aa2-899e-418e29ffbd36', 42, 'achievement.theTransporter.title', 'achievement.theTransporter.description', 'INCREMENTAL', 500, 'http://content.faforever.com/achievements/d38aec23-e487-4aa2-899e-418e29ffbd36.png', 'http://content.faforever.com/achievements/d38aec23-e487-4aa2-899e-418e29ffbd36.png', 'REVEALED', 10),
('eb1ee9ab-4828-417b-b3e8-c8281ee7a353', 43, 'achievement.whoNeedsSupport.title', 'achievement.whoNeedsSupport.description', 'INCREMENTAL', 10, 'http://content.faforever.com/achievements/eb1ee9ab-4828-417b-b3e8-c8281ee7a353.png', 'http://content.faforever.com/achievements/eb1ee9ab-4828-417b-b3e8-c8281ee7a353.png', 'REVEALED', 10),
('e7645e7c-7456-48a8-a562-d97521498e7e', 44, 'achievement.deadlyBugs.title', 'achievement.deadlyBugs.description', 'INCREMENTAL', 500, 'http://content.faforever.com/achievements/e7645e7c-7456-48a8-a562-d97521498e7e.png', 'http://content.faforever.com/achievements/e7645e7c-7456-48a8-a562-d97521498e7e.png', 'REVEALED', 10),
('f0cde5d8-4933-4074-a2fb-819074d21abd', 45, 'achievement.noMercy.title', 'achievement.noMercy.description', 'INCREMENTAL', 500, 'http://content.faforever.com/achievements/f0cde5d8-4933-4074-a2fb-819074d21abd.png', 'http://content.faforever.com/achievements/f0cde5d8-4933-4074-a2fb-819074d21abd.png', 'REVEALED', 10),
('a98fcfaf-29ac-4526-84c2-44f284518f8c', 46, 'achievement.flyingDeath.title', 'achievement.flyingDeath.description', 'INCREMENTAL', 50, 'http://content.faforever.com/achievements/a98fcfaf-29ac-4526-84c2-44f284518f8c.png', 'http://content.faforever.com/achievements/a98fcfaf-29ac-4526-84c2-44f284518f8c.png', 'REVEALED', 10),
('1c8fcb6f-a5b6-497f-8b0d-ac5ac6fef408', 47, 'achievement.incomingRobots.title', 'achievement.incomingRobots.description', 'INCREMENTAL', 50, 'http://content.faforever.com/achievements/1c8fcb6f-a5b6-497f-8b0d-ac5ac6fef408.png', 'http://content.faforever.com/achievements/1c8fcb6f-a5b6-497f-8b0d-ac5ac6fef408.png', 'REVEALED', 10),
('a1f87fb7-67ca-4a86-afc6-f23a41b40e9f', 48, 'achievement.arachnologist.title', 'achievement.arachnologist.description', 'INCREMENTAL', 50, 'http://content.faforever.com/achievements/a1f87fb7-67ca-4a86-afc6-f23a41b40e9f.png', 'http://content.faforever.com/achievements/a1f87fb7-67ca-4a86-afc6-f23a41b40e9f.png', 'REVEALED', 10),
('db141e87-5818-435f-80a3-08cc6f1fdac6', 49, 'achievement.holyCrab.title', 'achievement.holyCrab.description', 'INCREMENTAL', 50, 'http://content.faforever.com/achievements/db141e87-5818-435f-80a3-08cc6f1fdac6.png', 'http://content.faforever.com/achievements/db141e87-5818-435f-80a3-08cc6f1fdac6.png', 'REVEALED', 10),
('ab241de5-e773-412e-b073-090da8e38c4c', 50, 'achievement.fatterIsBetter.title', 'achievement.fatterIsBetter.description', 'INCREMENTAL', 50, 'http://content.faforever.com/achievements/ab241de5-e773-412e-b073-090da8e38c4c.png', 'http://content.faforever.com/achievements/ab241de5-e773-412e-b073-090da8e38c4c.png', 'REVEALED', 10),
('1f140add-b0ae-4e02-91a0-45d62b988a22', 51, 'achievement.alienInvasion.title', 'achievement.alienInvasion.description', 'INCREMENTAL', 50, 'http://content.faforever.com/achievements/1f140add-b0ae-4e02-91a0-45d62b988a22.png', 'http://content.faforever.com/achievements/1f140add-b0ae-4e02-91a0-45d62b988a22.png', 'REVEALED', 10),
('60d1e60d-036b-491e-a992-2b18321848c2', 52, 'achievement.assWasher.title', 'achievement.assWasher.description', 'INCREMENTAL', 50, 'http://content.faforever.com/achievements/60d1e60d-036b-491e-a992-2b18321848c2.png', 'http://content.faforever.com/achievements/60d1e60d-036b-491e-a992-2b18321848c2.png', 'REVEALED', 10),
('539da20b-5026-4c49-8e22-e4a305d58845', 53, 'achievement.deathFromAbove.title', 'achievement.deathFromAbove.description', 'INCREMENTAL', 50, 'http://content.faforever.com/achievements/539da20b-5026-4c49-8e22-e4a305d58845.png', 'http://content.faforever.com/achievements/539da20b-5026-4c49-8e22-e4a305d58845.png', 'REVEALED', 10),
('e603f306-ba6b-4507-9556-37a308e5a722', 54, 'achievement.stormySea.title', 'achievement.stormySea.description', 'INCREMENTAL', 50, 'http://content.faforever.com/achievements/e603f306-ba6b-4507-9556-37a308e5a722.png', 'http://content.faforever.com/achievements/e603f306-ba6b-4507-9556-37a308e5a722.png', 'REVEALED', 10),
('a909629f-46f5-469e-afd1-192d42f55e4d', 55, 'achievement.itAintACity.title', 'achievement.itAintACity.description', 'INCREMENTAL', 50, 'http://content.faforever.com/achievements/a909629f-46f5-469e-afd1-192d42f55e4d.png', 'http://content.faforever.com/achievements/a909629f-46f5-469e-afd1-192d42f55e4d.png', 'REVEALED', 10),
('50260d04-90ff-45c8-816b-4ad8d7b97ecd', 56, 'achievement.rainmaker.title', 'achievement.rainmaker.description', 'STANDARD', NULL, 'http://content.faforever.com/achievements/50260d04-90ff-45c8-816b-4ad8d7b97ecd.png', 'http://content.faforever.com/achievements/50260d04-90ff-45c8-816b-4ad8d7b97ecd.png', 'REVEALED', 10),
('31a728f8-ace9-45fa-a3f2-57084bc9e461', 57, 'achievement.iHaveACannon.title', 'achievement.iHaveACannon.description', 'STANDARD', NULL, 'http://content.faforever.com/achievements/31a728f8-ace9-45fa-a3f2-57084bc9e461.png', 'http://content.faforever.com/achievements/31a728f8-ace9-45fa-a3f2-57084bc9e461.png', 'REVEALED', 10),
('987ca192-26e1-4b96-b593-40c115451cc0', 58, 'achievement.makeItHail.title', 'achievement.makeItHail.description', 'STANDARD', NULL, 'http://content.faforever.com/achievements/987ca192-26e1-4b96-b593-40c115451cc0.png', 'http://content.faforever.com/achievements/987ca192-26e1-4b96-b593-40c115451cc0.png', 'REVEALED', 10),
('46a6e900-88bb-4eae-92d1-4f31b53faedc', 59, 'achievement.soMuchResources.title', 'achievement.soMuchResources.description', 'STANDARD', NULL, 'http://content.faforever.com/achievements/46a6e900-88bb-4eae-92d1-4f31b53faedc.png', 'http://content.faforever.com/achievements/46a6e900-88bb-4eae-92d1-4f31b53faedc.png', 'REVEALED', 10),
('9ad697bb-441e-45a5-b682-b9227e8eef3e', 60, 'achievement.nuclearWar.title', 'achievement.nuclearWar.description', 'STANDARD', NULL, 'http://content.faforever.com/achievements/9ad697bb-441e-45a5-b682-b9227e8eef3e.png', 'http://content.faforever.com/achievements/9ad697bb-441e-45a5-b682-b9227e8eef3e.png', 'REVEALED', 10),
('a6b7dfa1-1ebc-4c6d-9305-4a9d623e1b4f', 61, 'achievement.drEvil.title', 'achievement.drEvil.description', 'INCREMENTAL', 500, 'http://content.faforever.com/achievements/a6b7dfa1-1ebc-4c6d-9305-4a9d623e1b4f.png', 'http://content.faforever.com/achievements/a6b7dfa1-1ebc-4c6d-9305-4a9d623e1b4f.png', 'REVEALED', 10),
('2103e0de-1c87-4fba-bc1b-0bba66669607', 62, 'achievement.dontMessWitHme.title', 'achievement.dontMessWitHme.description', 'INCREMENTAL', 100, 'http://content.faforever.com/achievements/2103e0de-1c87-4fba-bc1b-0bba66669607.png', 'http://content.faforever.com/achievements/2103e0de-1c87-4fba-bc1b-0bba66669607.png', 'REVEALED', 10);




--
-- Dumping data for table `event_definitions`
--

TRUNCATE TABLE `event_definitions`;
INSERT INTO `event_definitions` (`id`, `name_key`, `image_url`, `type`) VALUES
('cfa449a6-655b-48d5-9a27-6044804fe35c', 'event.customGamesPlayed', NULL, 'NUMERIC'),
('4a929def-e347-45b4-b26d-4325a3115859', 'event.ranked1v1GamesPlayed', NULL, 'NUMERIC'),
('d6a699b7-99bc-4a7f-b128-15e1e289a7b3', 'event.lostAcus', NULL, 'NUMERIC'),
('3ebb0c4d-5e92-4446-bf52-d17ba9c5cd3c', 'event.builtAirUnits',  NULL, 'NUMERIC'),
('225e9b2e-ae09-4ae1-a198-eca8780b0fcd', 'event.lostAirUnits', NULL, 'NUMERIC'),
('ea123d7f-bb2e-4a71-bd31-88859f0c3c00', 'event.builtLandUnits', NULL, 'NUMERIC'),
('a1a3fd33-abe2-4e56-800a-b72f4c925825', 'event.lostLandUnits',  NULL, 'NUMERIC'),
('b5265b42-1747-4ba1-936c-292202637ce6', 'event.builtNavalUnits', NULL, 'NUMERIC'),
('3a7b3667-0f79-4ac7-be63-ba841fd5ef05', 'event.lostNavalUnits', NULL, 'NUMERIC'),
('cc791f00-343c-48d4-b5b3-8900b83209c0', 'event.secondsPlayed', NULL, 'TIME'),
('a8ee4f40-1e30-447b-bc2c-b03065219795', 'event.builtTech1Units', NULL, 'NUMERIC'),
('3dd3ed78-ce78-4006-81fd-10926738fbf3', 'event.lostTech1Units', NULL, 'NUMERIC'),
('89d4f391-ed2d-4beb-a1ca-6b93db623c04', 'event.builtTech2Units', NULL, 'NUMERIC'),
('aebd750b-770b-4869-8e37-4d4cfdc480d0', 'event.lostTech2Units', NULL, 'NUMERIC'),
('92617974-8c1f-494d-ab86-65c2a95d1486', 'event.builtTech3Units', NULL, 'NUMERIC'),
('7f15c2be-80b7-4573-8f41-135f84773e0f', 'event.lostTech3Units',  NULL, 'NUMERIC'),
('ed9fd79d-5ec7-4243-9ccf-f18c4f5baef1', 'event.builtExperimentals', NULL, 'NUMERIC'),
('701ca426-0943-4931-85af-6a08d36d9aaa', 'event.lostExperimentals', NULL, 'NUMERIC'),
('60bb1fc0-601b-45cd-bd26-83b1a1ac979b', 'event.builtEngineers', NULL, 'NUMERIC'),
('e8e99a68-de1b-4676-860d-056ad2207119', 'event.lostEngineers', NULL, 'NUMERIC'),
('96ccc66a-c5a0-4f48-acaa-888b00778b57', 'event.aeonPlays', NULL, 'NUMERIC'),
('a6b51c26-64e6-4e7a-bda7-ea1cfe771ebb', 'event.aeonWins', NULL, 'NUMERIC'),
('ad193982-e7ca-465c-80b0-5493f9739559', 'event.cybranPlays', NULL, 'NUMERIC'),
('56b06197-1890-42d0-8b59-25e1add8dc9a', 'event.cybranWins', NULL, 'NUMERIC'),
('1b900d26-90d2-43d0-a64e-ed90b74c3704', 'event.uefPlays',  NULL, 'NUMERIC'),
('7be6fdc5-7867-4467-98ce-f7244a66625a', 'event.uefWins', NULL, 'NUMERIC'),
('fefcb392-848f-4836-9683-300b283bc308', 'event.seraphimPlays', NULL, 'NUMERIC'),
('15b6c19a-6084-4e82-ada9-6c30e282191f', 'event.seraphimWins', NULL, 'NUMERIC');


--
-- Dumping data for table `messages`
--

TRUNCATE TABLE `messages`;
INSERT INTO `messages` (`key`, `language`, `region`, `value`) VALUES
('achievement.rusher.title', 'en', 'US', 'Rusher'),
('achievement.rusher.description', 'en', 'US', 'Kill your enemy in a ranked 1v1 game in under 15 minutes'),
('achievement.whatASwarm.title', 'en', 'US', 'What a swarm'),
('achievement.whatASwarm.description', 'en', 'US', 'Build 500 Air Superiority Fighter in a single game'),
('achievement.blaze.title', 'en', 'US', 'Blaze'),
('achievement.blaze.description', 'en', 'US', 'Win 25 games with Aeon'),
('achievement.techie.title', 'en', 'US', 'Techie'),
('achievement.techie.description', 'en', 'US', 'Win 5 games with high usage of experimentals (3 or more)'),
('achievement.hattrick.title', 'en', 'US', 'Hattrick'),
('achievement.hattrick.description', 'en', 'US', 'Kill 3 enemies in one game and survive'),
('achievement.fieldMarshal.title', 'en', 'US', 'Field marshal'),
('achievement.fieldMarshal.description', 'en', 'US', 'Win 50 games with land dominant army'),
('achievement.ma12Striker.title', 'en', 'US', 'MA12 Striker'),
('achievement.ma12Striker.description', 'en', 'US', 'Win 5 games with UEF'),
('achievement.incomingRobots.title', 'en', 'US', 'Incoming robots'),
('achievement.incomingRobots.description', 'en', 'US', 'Build 50 Galactic Colossus in total'),
('achievement.alienInvasion.title', 'en', 'US', 'Alien invasion'),
('achievement.alienInvasion.description', 'en', 'US', 'Build 50 Ythothas in total'),
('achievement.dontMessWitHme.title', 'en', 'US', 'Don''t mess with me'),
('achievement.dontMessWitHme.description', 'en', 'US', 'Kill 100 ACUs in total'),
('achievement.thatWasClose.title', 'en', 'US', 'That was close'),
('achievement.thatWasClose.description', 'en', 'US', 'Survive despite your ACU\'s health got below 500hp'),
('achievement.seaman.title', 'en', 'US', 'Seaman'),
('achievement.seaman.description', 'en', 'US', 'Win 25 games with navy dominant army'),
('achievement.topScore.title', 'en', 'US', 'Top score'),
('achievement.topScore.description', 'en', 'US', 'Be the top scoring player in a game with at least 8 players.'),
('achievement.iHaveACannon.title', 'en', 'US', 'I have a cannon'),
('achievement.iHaveACannon.description', 'en', 'US', 'Win a game building a Mavor'),
('achievement.Riptide.title', 'en', 'US', 'Riptide'),
('achievement.Riptide.description', 'en', 'US', 'Win 25 games with UEF'),
('achievement.soMuchResources.title', 'en', 'US', 'So much resources'),
('achievement.soMuchResources.description', 'en', 'US', 'Win a game building a Paragon'),
('achievement.rainmaker.title', 'en', 'US', 'Rainmaker'),
('achievement.rainmaker.description', 'en', 'US',  'Win a game building a Salvation'),
('achievement.landLubber.title', 'en', 'US', 'Landlubber'),
('achievement.landLubber.description', 'en', 'US', 'Win 5 games with navy dominant army'),
('achievement.deathFromAbove.title', 'en', 'US', 'Death from above'),
('achievement.deathFromAbove.description', 'en', 'US', 'Build 50 CZARs in total'),
('achievement.firstSuccess.title', 'en', 'US', 'First success'),
('achievement.firstSuccess.description', 'en', 'US', 'Win a 1v1 ranked game'),
('achievement.assWasher.title', 'en', 'US', 'Ass washer'),
('achievement.assWasher.description', 'en', 'US', 'Build 50 Ahwassas in total'),
('achievement.senior.title', 'en', 'US', 'Senior'),
('achievement.senior.description', 'en', 'US', 'Play 250 games'),
('achievement.suthanus.title', 'en', 'US', 'Suthanus'),
('achievement.suthanus.description', 'en', 'US', 'Win 50 games with Seraphim'),
('achievement.yenzyne.title', 'en', 'US', 'Yenzyne'),
('achievement.yenzyne.description', 'en', 'US', 'Win 25 games with Seraphim'),
('achievement.demolisher.title', 'en', 'US', 'Demolisher'),
('achievement.demolisher.description', 'en', 'US', 'Win 50 games with UEF'),
('achievement.serenity.title', 'en', 'US', 'Serenity'),
('achievement.serenity.description', 'en', 'US', 'Win 50 games with Aeon'),
('achievement.makeItHail.title', 'en', 'US', 'Make it hail'),
('achievement.makeItHail.description', 'en', 'US', 'Win a game building a Scathis'),
('achievement.nuclearWar.title', 'en', 'US', 'Nuclear war'),
('achievement.nuclearWar.description', 'en', 'US', 'Win a game building a Yolona Oss'),
('achievement.arachnologist.title', 'en', 'US', 'Arachnologist'),
('achievement.arachnologist.description', 'en', 'US', 'Build 50 Monkeylords in total'),
('achievement.wingman.title', 'en', 'US', 'Wingman'),
('achievement.wingman.description', 'en', 'US', 'Win 25 games with air dominant army'),
('achievement.drEvil.title', 'en', 'US', 'Dr. Evil'),
('achievement.drEvil.description', 'en', 'US', 'Build 500 experimentals in total'),
('achievement.itAintACity.title', 'en', 'US', 'It ain''t a city'),
('achievement.itAintACity.description', 'en', 'US', 'Build 50 Atlantis in total'),
('achievement.flyingDeath.title', 'en', 'US', 'Flying death'),
('achievement.flyingDeath.description', 'en', 'US', 'Build 50 soul rippers in total'),
('achievement.fatterIsBetter.title', 'en', 'US', 'Fatter is better'),
('achievement.fatterIsBetter.description', 'en', 'US', 'Build 50 Fatboys in total'),
('achievement.wagner.title', 'en', 'US', 'Wagner'),
('achievement.wagner.description', 'en', 'US', 'Win 25 games with Cybran'),
('achievement.veteran.title', 'en', 'US', 'Veteran'),
('achievement.veteran.description', 'en', 'US', 'Play 500 games'),
('achievement.addict.title', 'en', 'US', 'Addict'),
('achievement.addict.description', 'en', 'US', 'Play 1000 games'),
('achievement.admiralOfTheFleet.title', 'en', 'US', 'Admiral of the fleet'),
('achievement.admiralOfTheFleet.description', 'en', 'US', 'Win 50 games with navy dominant army'),
('achievement.wrightBrother.title', 'en', 'US', 'Wright brother'),
('achievement.wrightBrother.description', 'en', 'US', 'Win 5 games with air dominant army'),
('achievement.novice.title', 'en', 'US', 'Novice'),
('achievement.novice.description', 'en', 'US', 'Play 10 games'),
('achievement.thaam.title', 'en', 'US', 'Thaam'),
('achievement.thaam.description', 'en', 'US', 'Win 5 games with Seraphim'),
('achievement.iLoveBigToys.title', 'en', 'US', 'I love big toys'),
('achievement.iLoveBigToys.description', 'en', 'US', 'Win 25 games with high usage of experimentals (3 or more)'),
('achievement.mantis.title', 'en', 'US', 'Mantis'),
('achievement.mantis.description', 'en', 'US', 'Win 5 games with Cybran'),
('achievement.theTransporter.title', 'en', 'US', 'The transporter'),
('achievement.theTransporter.description', 'en', 'US', 'Build 500 transporters in total'),
('achievement.unbeatable.title', 'en', 'US', 'Unbeatable'),
('achievement.unbeatable.description', 'en', 'US', 'Be the top scoring player 10 times in games with at least 8 players.'),
('achievement.junior.title', 'en', 'US', 'Junior'),
('achievement.junior.description', 'en', 'US', 'Play 50 games'),
('achievement.aurora.title', 'en', 'US', 'Aurora'),
('achievement.aurora.description', 'en', 'US', 'Win 5 games with Aeon'),
('achievement.holyCrab.title', 'en', 'US', 'Holy crab'),
('achievement.holyCrab.description', 'en', 'US', 'Build 50 Megaliths in total'),
('achievement.kingOfTheSkies.title', 'en', 'US', 'King of the skies'),
('achievement.kingOfTheSkies.description', 'en', 'US', 'Win 50 games with air dominant army'),
('achievement.militiaman.title', 'en', 'US', 'Militiaman'),
('achievement.militiaman.description', 'en', 'US', 'Win 5 games with land dominant army'),
('achievement.stormySea.title', 'en', 'US', 'Stormy sea'),
('achievement.stormySea.description', 'en', 'US', 'Build 50 Tempests in total'),
('achievement.deadlyBugs.title', 'en', 'US', 'Deadly bugs'),
('achievement.deadlyBugs.description', 'en', 'US', 'Build 500 fire beetles in total'),
('achievement.experimentalist.title', 'en', 'US', 'Experimentalist'),
('achievement.experimentalist.description', 'en', 'US', 'Win 50 games with high usage of experimentals (3 or more)'),
('achievement.whoNeedsSupport.title', 'en', 'US', 'Who needs support?'),
('achievement.whoNeedsSupport.description', 'en', 'US', 'Build 10 Support Command Units in a single game'),
('achievement.grenadier.title', 'en', 'US', 'Grenadier'),
('achievement.grenadier.description', 'en', 'US', 'Win 25 games with land dominant army'),
('achievement.noMercy.title', 'en', 'US', 'No mercy'),
('achievement.noMercy.description', 'en', 'US', 'Build 500 Mercies in total'),
('achievement.trebuchet.title', 'en', 'US', 'Trebuchet'),
('achievement.trebuchet.description', 'en', 'US', 'Win 50 games with Cybran'),
('event.seraphimWins', 'en', 'US', 'Seraphim wins'),
('event.uefPlays', 'en', 'US', 'UEF plays'),
('event.lostAirUnits', 'en', 'US', 'Lost air units'),
('event.lostNavalUnits', 'en', 'US', 'Lost naval units'),
('event.lostTech1Units', 'en', 'US', 'Lost tech 1 units'),
('event.builtAirUnits', 'en', 'US', 'Built air units'),
('event.ranked1v1GamesPlayed', 'en', 'US', 'Ranked 1v1 games played'),
('event.cybranWins', 'en', 'US', 'Cybran wins'),
('event.builtEngineers', 'en', 'US', 'Built engineers'),
('event.lostExperimentals', 'en', 'US', 'Lost experimentals'),
('event.uefWins', 'en', 'US', 'UEF wins'),
('event.lostTech3Units', 'en', 'US', 'Lost tech 3 units'),
('event.builtTech2Units', 'en', 'US', 'Built tech 2 units'),
('event.builtTech3Units', 'en', 'US', 'Built tech 3 units'),
('event.aeonPlays', 'en', 'US', 'Aeon plays'),
('event.lostLandUnits', 'en', 'US', 'Lost land units'),
('event.aeonWins', 'en', 'US', 'Aeon wins'),
('event.builtTech1Units', 'en', 'US', 'Built tech 1 units'),
('event.cybranPlays', 'en', 'US', 'Cybran plays'),
('event.lostTech2Units', 'en', 'US', 'Lost tech 2 units'),
('event.builtNavalUnits', 'en', 'US', 'Built naval units'),
('event.secondsPlayed', 'en', 'US', 'Seconds played'),
('event.customGamesPlayed', 'en', 'US', 'Custom games played'),
('event.lostAcus', 'en', 'US', 'Lost ACUs'),
('event.lostEngineers', 'en', 'US', 'Lost engineers'),
('event.builtLandUnits', 'en', 'US', 'Built land units'),
('event.builtExperimentals', 'en', 'US', 'Built experimentals'),
('event.seraphimPlays', 'en', 'US', 'Seraphim plays');


--
-- Dumping data for table `oauth_clients`
--

TRUNCATE TABLE `oauth_clients`;
INSERT INTO `oauth_clients` (`id`, `name`, `client_secret`, `client_type`, `redirect_uris`, `default_redirect_uri`, `default_scope`, `icon_url`) VALUES
('3bc8282c-7730-11e5-8bcf-feff819cdc9f', 'Downlord\'s FAF Client', '6035bd78-7730-11e5-8bcf-feff819cdc9f', 'public', 'http://localhost', 'http://localhost', 'read_events read_achievements upload_map', '');
