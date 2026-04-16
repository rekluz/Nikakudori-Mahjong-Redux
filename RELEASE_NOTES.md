# Release Notes - v5.5.2

## Changes Since V5.3.1

### New Features
- **3D Board Layouts** - Added 6 new layered board layouts (Pyramid, Fortress, Turtle, Bridge, Dragon, Castle)
- **Pinch-to-Zoom** - Pinch gestures to zoom the game board
- **Welcome Screen** - New animated welcome screen with petal particle effects
- **Music System** - Background music with lifecycle-aware playback

### Improvements
- **Refactored GameViewModel** - Split into dedicated use cases (AutoComplete, Hint, Shuffle, Undo, InteractionCoordinator)
- **PathFinder Refactoring** - Improved path-finding algorithm for tile matching
- **HD Tile Assets** - Added high-resolution tile set for xxxhdpi displays
- **Background Images** - Added new scenic backgrounds with metadata overlay

### Bug Fixes
- Fixed 3D board tile placement for odd/even tile counts
- Fixed 3D tile generation logic
- Fixed music playback issues
- Removed warning notifications
- Fixed debug text in background image overlay

### Code Quality
- Added unit tests for GameViewModel, GameEngine, BoardGenerator, HintFinder, Difficulty
- Updated Gradle and dependency versions

---

## Version History

| Version | Date | Highlights |
|---------|------|------------|
| V5.3.1 | - | Previous release |
| V5.5.2 | Apr 2026 | Current release |

---

*Copyright © 2026 Rekluz Games. All rights reserved.*