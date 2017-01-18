ALTER TABLE oauth_clients
  ADD auto_approve_scopes TEXT NULL COMMENT 'A space delimited list of scopes that don''t need to be approved by the user.';
