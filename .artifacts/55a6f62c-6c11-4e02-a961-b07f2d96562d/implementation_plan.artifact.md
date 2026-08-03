# Implementation Plan - Workout Timer & Completion Redesign

Upgrade the Workout Timer and Completion UI to the premium **"3D Obsidian Dark Metallic / Leather Neumorphic"** aesthetic.

## User Review Required

> [!IMPORTANT]
> This redesign replaces the vibrant purple/cyan theme with a high-contrast metallic and obsidian void look. The "Timer" and "Stopwatch" dialogs will now feature 3D sweep-gradient rings and transparent metallic-outlined buttons.

## Proposed Changes

### UI Components

#### [MODIFY] [WorkoutTimer.kt](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/ui/components/WorkoutTimer.kt)
- **Timer & Stopwatch Dialogs**:
    - Update background to `ObsidianVoid` (98% opacity for slight transparency).
    - **3D Metallic Ring**: Implement `Brush.sweepGradient` using `ChromeSilver`, `DarkSteel`, `MutedSlate` for the progress indicator.
    - **Recessed Track**: Use `ObsidianVoid` with a 1dp silver border for the ring's background track.
    - **Typography**: Change timer digits to Stark White with a subtle silver shadow/glow.
- **`TimerButton`**:
    - Redesign as a transparent metallic outlined button.
    - Background: `Color.Black.copy(alpha = 0.3f)`.
    - Border: 1dp Chrome/Silver gradient stroke.
    - Text: Bold Metallic White.
    - Shared height: 48dp.
- **`PremiumCompletionPanel`**:
    - Wrap content in `ExorkNeumorphicCard` with 1.5dp metallic bezel.
    - Background: Translucent Charcoal Leather (`Color(0xCC121215)`).
    - Icon: Metallic Silver/White checkmark icon.
    - Action: Replace standard button with `ExorkChromeButton` ("MARK SET COMPLETE").

## Verification Plan

### Automated Tests
- Build project to ensure no compilation errors.

### Manual Verification
- **Timer Flow**:
    - Start a timed exercise (e.g., Plank).
    - Verify the 3D metallic ring animation and silver digits.
    - Verify "PAUSE", "RESUME", and "CANCEL" buttons match the transparent outlined style.
- **Completion Overlay**:
    - Finish a set.
    - Verify the completion card uses the leather texture, chrome bezel, and raised metallic button.
