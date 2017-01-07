ALTER TABLE game_featuredMods
    ADD COLUMN `git_url` varchar(255) COMMENT 'The git repository URL to load this mod from',
    ADD COLUMN `git_branch` varchar(255) COMMENT 'The repository branch that contains the latest version';