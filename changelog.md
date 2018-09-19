# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Changed
- Custom ICE server credentials now use long term credentials as specified in RFC 5389

### Fixed
- Don't require login for actuator endpoints

## v1.3.4
### Added
- Added player verification message
- Mark games as unranked when they have at least one AI
- The v2 protocol documentation is now available at `/v2-protocol.html`

### Changed
- Replaced UID verification with integration of the [FAF Policy Server](https://github.com/FAForever/faf-policy-server).
The environment variables `UID_ENABLED` and `UID_PRIVATE_KEY` have been removed, `POLICY_SERVICE_URL` has been added.
If no policy service URL is specified, the service is disabled.
- If ladder1v1 or coop isn't available in the database, the server will now start anyway but log warn messages.
These game modes will then not be available and an error will be sent to the client if it tries to use them.

### Fixed
- Fixed missing `data` block in v2 protocol documentation
- Games reported as password protected when they were not
- Respect permanent as well as temporary bans correctly
- Don't throw error when receiving a `Disconnect` message. No behavior is implemented (because not needed) but a debug 
statement is logged
- (Player) clients can now log in using the v2 protocol
- Close open games when its host leaves
- Properly escape single quotes `'` in standard error messages
