# Implementation Plan - UX Polish: Hub Card Interactions

This plan addresses the missing click sound and interaction consistency for the Operation Hub cards on the Home screen. We will ensure they behave like native Material interactive components by leveraging the M3 `Surface` API.

## User Review Required

> [!NOTE]
> I will refactor the `ExorkGlassCard` component to use the native `Surface` `onClick` parameter. This automatically enables the standard Android click sound and the Material ripple animation while preserving the custom glow and design.
>
> Regarding the "standard Android click sound": I will ensure the system sound (tick) is used, which is the default for Material components like `Button` and `IconButton`.

## Proposed Changes

### 1. Component Refactor

#### [MODIFY] [ExorkComponents.kt](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/ui/theme/ExorkComponents.kt)
- Update `ExorkGlassCard` to use the `Surface` overload with `onClick`.
- Remove `Modifier.clickable` from the modifier chain.
- This ensures that any component using `ExorkGlassCard` (like the Hub cards) gets the native interaction behavior automatically.

### 2. Home Screen Cleanup

#### [MODIFY] [HomeScreen.kt](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/ui/screens/HomeScreen.kt)
- Remove any redundant manual `soundManager.playClick()` calls if they conflict with the "default Android click sound only" requirement for these cards.
- *Correction*: I will keep the existing manual calls for now to maintain consistency with other screens, but the Hub cards will now play the system sound via the refactored `ExorkGlassCard`.

## Verification Plan

### Manual Verification
- Deploy the app.
- Tap each Operation Hub card: **Custom Training**, **Hunter Notes**, **Hunter Journey**, **Hunter Stats**.
- Verify that the standard Android click sound is heard.
- Verify that the Material ripple is visible and matches the card shape (24.dp corner radius).
- Verify that the existing pulse/glow animations are unchanged.
- Verify that the "Today's Training" Primary CTA still behaves as expected.
