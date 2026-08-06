# Changelog

All notable changes to this project will be documented in this file.

## [1.3.0] - 2025-02-15

### Added
- Created a GitHub Actions workflow `.github/workflows/release.yml` for building and signing release APKs and releasing them automatically on branch merges to the `master` branch.
- Configured dynamic APK versioning based on creation date (`YYYY.MM.DD.hh.mm`).

### Changed
- Set Calendar as the default and only main screen (removed bottom navigation entirely).
- Made the Month mode the default view on the calendar.
- Integrated the application settings menu directly into the top bar of the Calendar Screen.

### Removed
- Removed the Home screen (`TodayScreen`).
- Removed the Widget system entirely, including resource XML files, receivers, and managers.
- Removed the Event management feature from everywhere in the app, leaving it purely as a Task manager.
- Removed the bottom task/event switcher tab bar from the calendar.
