# Implementation Plan - Fix Navigation Black Screen Bug

The user reported a bug where rapid back-taps on the Hunter Profile screen eventually lead to a black screen. This is caused by the `startDestination` (Home) being popped from the back stack, leaving the `NavHost` empty.

## User Review Required

> [!IMPORTANT]
> The fix involves modifying the core navigation handling in `MainActivity.kt`. It adds state checks to prevent duplicate navigation and ensures the start destination is never popped from the UI back buttons.

## Proposed Changes

### [MainActivity](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/MainActivity.kt)

#### [MODIFY] [MainActivity.kt](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/MainActivity.kt)
- Enhance `safeNav` to check if the current backstack entry is in the `RESUMED` state before performing any action. This prevents multiple navigation events during a single transition.
- Implement `safePop` and `safeNavigate` helper functions.
- `safePop` will check `navController.previousBackStackEntry != null` to ensure we never pop the last destination (Home).
- `safeNavigate` will use `launchSingleTop = true` to prevent duplicate screens if the user taps a navigation button multiple times.
- Update all `composable` blocks in `NavHost` to use these new helpers.

## Verification Plan

### Manual Verification
- **Rapid Tap Test**: Open Hunter Profile. Tap the back arrow 50+ times as fast as possible.
  - Verify: App returns to Home and stays there. No black screen.
- **Duplicate Screen Test**: Rapidly tap "Profile" or "Archives" from the Home screen.
  - Verify: Only one instance of the destination is added to the stack. (Pressing back once returns to Home).
- **Home Back Test**: Verify that pressing the System Back button on Home still follows default Android behavior (exiting/backgrounding) while the UI back buttons are guarded.

### Automated Tests
- Run `gradlew assembleDebug` to ensure no compilation errors.
