# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com)
and this project adheres to
[Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- This library has been renamed to `liberator.resource.ping`.
- Rather than providing a message for the ping response body, now a `:body`
  key can be provided, either as data or a function of context returning data,
  used as the properties on the resulting ping resource.
- Rather than `handler` accepting an `options` map, it now accepts an
  `overrides` map, used as the most specific definition map when building the
  resource. As such, anything that would normally go into a liberator resource
  definition map can be provided.

## [0.1.2] — 2020-03-29

### Added

- The ping resource now has a HAL response body, including links to self and
  discovery.

## [0.1.1] — 2020-03-22

### Added

- Populated _CHANGELOG.md_ with all releases to date.

## 0.1.1-SNAPSHOT — 2020-03-22

Released without _CHANGELOG.md_.


[0.1.1]: https://github.com/logicblocks/liberator.resource.ping/compare/0.1.1-SNAPSHOT...0.1.1
[0.1.2]: https://github.com/logicblocks/liberator.resource.ping/compare/0.1.1...0.1.2
[Unreleased]: https://github.com/logicblocks/liberator.resource.ping/compare/0.1.2...HEAD
