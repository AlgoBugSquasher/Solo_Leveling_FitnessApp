# Implementation Plan - Multi-Exercise Daily Quest System Refactor

Refactor the Daily Quest system to feature a unified 3-exercise mission with dynamic randomized targets and a cinematic app-launch notification.

## User Review Required

> [!IMPORTANT]
> This refactor changes the core logic of daily quests. Quests will no longer be randomized individual tasks but a fixed set of three (Push-ups, Sit-ups, Plank) with a shared target value.

## Proposed Changes

### Data Layer

#### [MODIFY] [DailyQuest.kt](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/model/DailyQuest.kt)
- Add `currentProgress: Int` and `targetValue: Int` fields to the `DailyQuest` entity.
- Remove sets/reps logic in favor of the unified target system.

### Business Logic

#### [MODIFY] [HomeViewModel.kt](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/viewmodel/HomeViewModel.kt)
- **Quest Generation**: Update `checkAndRefreshQuests` to:
    - Select a random 'X' from `listOf(15, 20, 25, 30)`.
    - Generate 3 `DailyQuest` objects: PUSH-UPS, SIT-UPS, PLANK, all with `targetValue = X`.
- **Progress Tracking**: Update `completeQuest` (or add `updateQuestProgress`) to handle incremental progress for these 3 specific exercises.
- **Launch Logic**: Add a `shouldShowQuestDialog: StateFlow<Boolean>` that triggers true on the first launch of a new day.

### UI Layer

#### [NEW] [QuestInfoDialog.kt](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/ui/components/QuestInfoDialog.kt)
Create a new cinematic pop-up:
- **Style**: Translucent Dark Glass (`Color(0xCC0D0D12)`) with a 1.5dp Chrome Bezel.
- **Content**: Vertical list of the 3 exercises with `[current / target]` progress and the specified warning text.
- **Action**: 1dp Chrome Outlined "ACCEPT / START QUEST" button.

#### [MODIFY] [HomeScreen.kt](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/ui/screens/HomeScreen.kt)
- **Featured Mission**: Redesign the mission card to show all 3 exercises stacked vertically with live progress trackers.
- **Dialog Integration**: Observe `shouldShowQuestDialog` from the ViewModel and display the `QuestInfoDialog`.

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors.
- Verify quest generation logic selects from the correct target pool.

### Manual Verification
- **App Launch**: Verify the "QUEST INFO" pop-up appears after the splash screen on a new day.
- **Dialog Visuals**: Confirm the translucent glass background and metallic borders.
- **Home Screen**: Verify the Featured Mission card displays all 3 exercises with correct `[0 / X]` initial progress.
- **Progress Sync**: Complete a workout and verify the progress updates in real-time on the Home Screen card.
