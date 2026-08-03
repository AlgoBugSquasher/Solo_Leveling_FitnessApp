# Implementation Plan - Threading Fix for Backup & Restore

This plan fixes the "Cannot access database on the main thread" crash and a logic bug in the export process by ensuring all database and I/O operations are executed on background threads (`Dispatchers.IO`).

## User Review Required

> [!IMPORTANT]
> The "Create Backup" and "Restore Backup" operations will now be asynchronous. The UI will remain responsive, and progress will be reported via snackbars as before.

## Proposed Changes

### [Data Layer](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/data)

#### [MODIFY] [FitnessRepository.kt](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/data/FitnessRepository.kt)
- Refactor `clearAllDatabase` and `restoreDatabase` to remove `runBlocking`.
- Ensure these methods are marked `suspend` and perform their operations within the database's transaction context.

### [ViewModel Layer](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/viewmodel)

#### [MODIFY] [HomeViewModel.kt](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/viewmodel/HomeViewModel.kt)
- **`exportData`**: Change signature to `suspend fun exportData(): String`.
- Wrap the entire logic in `withContext(Dispatchers.IO)` to ensure it runs off the main thread and waits for all data to be collected before returning the JSON string.
- **`importData`**: Change `viewModelScope.launch { ... }` to `viewModelScope.launch(Dispatchers.IO) { ... }`.
- Post completion events (`BackupSuccess`, `BackupError`) back to the UI.

### [UI Layer](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/ui/screens)

#### [MODIFY] [SettingsScreen.kt](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/ui/screens/SettingsScreen.kt)
- Update the `createBackupLauncher` lambda to launch a coroutine using `rememberCoroutineScope()` to call the now-suspend `viewModel.exportData()`.
- Ensure file writing operations are performed within the same background scope.

## Verification Plan

### Automated Tests
- Verify project builds successfully.

### Manual Verification
1. **Create Backup**:
    - Trigger "Create Backup".
    - Verify no crash occurs.
    - Inspect the resulting file to ensure it contains ALL data (previously it might have been empty/partial due to the `launch` bug).
2. **Restore Backup**:
    - Trigger "Restore Backup".
    - Verify no crash occurs.
    - Confirm all data is correctly repopulated in the UI.
