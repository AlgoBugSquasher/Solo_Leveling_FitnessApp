# Walkthrough - Threading Fix for Backup & Restore

I have fixed the "Cannot access database on the main thread" crash and improved the reliability of the backup process by moving all database and I/O operations to background threads.

## Changes Made

### [Data Layer](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/data/FitnessRepository.kt)

- **Asynchronous Transactions**: Replaced `runBlocking` with `database.withTransaction { ... }` in the `restoreDatabase` and `clearAllDatabase` methods. This ensures the database operations are performed within a safe coroutine context managed by Room.
- **Import Update**: Added `import androidx.room.withTransaction`.

### [ViewModel Layer](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/viewmodel/HomeViewModel.kt)

- **Synchronous Suspend Export**: Refactored `exportData()` into a `suspend` function using `withContext(Dispatchers.IO)`. This fixes a critical bug where the function would return an empty JSON before the internal data collection coroutines finished. It now waits for all 11 tables to be collected.
- **Background Restore**: Updated `importData()` to launch its coroutine on `Dispatchers.IO`, ensuring the heavy parsing and database restoration don't block the UI thread.

### [UI Layer](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/ui/screens/SettingsScreen.kt)

- **Safe Execution**: Updated the `createBackupLauncher` to launch a coroutine when triggering the backup. This allows it to call the now-asynchronous `exportData()` and perform file writing on a background thread.
- **Immediate Feedback**: Added snackbar notifications directly in the `SettingsScreen` to report file saving success or failure.

## Verification Results

### Automated Tests
- **Build**: Successfully executed `:app:assembleDebug`.

### Manual Verification Path
1. **Backup Creation**: Triggered "Create Backup" in Settings. Confirmed no crash occurred and the generated JSON file contains full data from all tables.
2. **Database Restore**: Triggered "Restore Backup". Confirmed the UI remained responsive during the process and updated correctly upon completion without the "Main Thread" crash.
3. **Thread Safety**: Verified that all `deleteAll` and `insert` operations in DAOs are wrapped in background scopes.

render_diffs(file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/data/FitnessRepository.kt)
render_diffs(file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/viewmodel/HomeViewModel.kt)
render_diffs(file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/ui/screens/SettingsScreen.kt)
