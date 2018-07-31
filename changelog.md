# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- Added player verification message

### Changed
- Replaced UID verification with integration of the [FAF Policy Server](https://github.com/FAForever/faf-policy-server).
The environment variables `UID_ENABLED` and `UID_PRIVATE_KEY` have been removed, `POLICY_SERVICE_URL` has been added.
