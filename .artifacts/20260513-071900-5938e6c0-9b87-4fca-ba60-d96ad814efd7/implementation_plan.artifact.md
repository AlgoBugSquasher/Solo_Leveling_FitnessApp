# Implementation Plan - Hunter Archive UI Polish

Polish the Hunter Archive UI to feel premium, game-like, and visually immersive using rarity-based styling, enhanced card layouts, and smooth animations.

## User Review Required

> [!NOTE]
> I will be adding a `userLevel` state to the `HunterArchiveScreen` to calculate progress for locked badges. This will be collected from the `BadgeViewModel`.

## Proposed Changes

### UI Components

#### [HunterArchiveScreen.kt](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/ui/screens/HunterArchiveScreen.kt)

- **Enhance `BadgeCard`**:
    - Add rarity-based background gradients and glow effects.
    - Implement smooth scale and glow animations on press.
    - Improve text hierarchy and spacing.
    - Add a progress bar for locked badges.
- **Update `HunterArchiveScreen`**:
    - Collect current user level to pass to `BadgeCard`.
    - Refine the overall theme with a consistent dark purple palette.

### Data & ViewModel

#### [BadgeViewModel.kt](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/viewmodel/BadgeViewModel.kt)

- Expose the current user level as a `StateFlow` to be used by the UI for progress calculations.

---

## Verification Plan

### Automated Tests
- I will verify that the project builds successfully by running:
  `./gradlew assembleDebug`

### Manual Verification
- I will use `render_compose_preview` (if applicable) or rely on visual inspection of the code changes to ensure:
    - Rarity colors match the requirements (Common: Gray, Rare: Blue/Purple, Epic: Glowing Purple, Legendary: Neon Purple + Gold).
    - Locked badges show a progress bar and the "Reach Level X" text.
    - Unlocked badges have the animated glow and premium styling.
    - Pressing a badge triggers the scale animation.
