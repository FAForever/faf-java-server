ALTER TABLE achievement_definitions ADD create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE achievement_definitions ADD update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Set to the date when achievements started working
update achievement_definitions set create_time = '2016-08-08T06:00', update_time = '2016-08-08T06:00';

-- Except for those two, which where just added with the server update
update achievement_definitions set create_time = '2016-10-10T06:00', update_time = '2016-10-10T06:00'
    where id in ('d3d2c78b-d42d-4b65-99b8-a350f119f898', '305a8d34-42fd-42f3-ba91-d9f5e437a9a6');