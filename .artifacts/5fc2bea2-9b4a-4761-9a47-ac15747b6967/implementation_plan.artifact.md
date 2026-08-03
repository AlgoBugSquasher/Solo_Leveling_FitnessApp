# Restore Native Click Sound and Ripple for Operation Hub Cards

The goal is to restore the native Android click sound and ripple effects to the "Operation Hub" cards in the `HomeScreen` while ensuring the UI remains pixel-identical.

## User Review Required

> [!IMPORTANT]
> The fix involves modifying the core `ExorkGlassCard` component to use the Material 3 `Surface` with `onClick` parameter. This is the idiomatic way to ensure consistent interaction behavior (ripples and sounds) in Jetpack Compose.

## Proposed Changes

### [UI Components]

#### [MODIFY] [ExorkComponents.kt](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/ui/theme/ExorkComponents.kt)

- Refactor `ExorkGlassCard` to use the `Surface` overload that accepts an `onClick` parameter when a callback is provided.
- This ensures that the interaction is handled by the Material 3 `Surface` internal logic, which correctly manages the `InteractionSource`, ripple effects, and haptic/audio feedback (click sound).
- Existing properties like `shape`, `color`, `border`, and `tonalElevation` will be strictly preserved to maintain visual parity.

## Verification Plan

### Automated Tests
- Build the project to ensure no regressions in component signatures: `./gradlew assembleDebug`

### Manual Verification
- Deploy the app to a device/emulator.
- Navigate to the Home Screen.
- Tap each card in the "Operation Hub" (Custom Training, Hunter Notes, Hunter Journey, Hunter Stats).
- **Verify Click Sound:** Confirm that the standard Android click sound plays on each tap.
- **Verify Ripple Effect:** Confirm that the native ripple effect is visible and correctly constrained to the card's rounded corners.
- **Verify Visual Consistency:** Compare the UI before and after the change to ensure zero changes in layout, spacing, colors, or typography.
