# BlueBreath Tasker Setup

Use Tasker to launch BlueBreath and start the built-in 16-rep timer when you long-press `Volume Down`.

## Prerequisites

- BlueBreath installed on the phone
- Tasker installed
- Tasker granted the special `SET_VOLUME_KEY_LONG_PRESS_LISTENER` permission
- If you use LineageOS or similar, disable the ROM's own screen-off volume long-press handling

## Tasker Profile

1. Open Tasker.
2. Create a new `Profile`.
3. Choose `Event`.
4. Choose `Tasker`.
5. Choose `Volume Long Press`.
6. Set `Type` to `Volume Down`.
7. Leave `Additional Time` empty unless you want a longer hold.

## Tasker Task

Attach a task with one of these options.

### Option 1: Send Intent

- Action: `de.irmo.bluebreath.action.START_ASSIST_TIMER`
- Package: `de.irmo.bluebreath`
- Class: `de.irmo.bluebreath.MainActivity`
- Target: `Activity`

### Option 2: Run Shell

Command:

```sh
am start -a de.irmo.bluebreath.action.START_ASSIST_TIMER -n de.irmo.bluebreath/.MainActivity
```

Enable `Use Root` only if your Tasker setup needs it.

## What This Does

- Opens BlueBreath
- Starts a breathing session immediately
- Uses `16` reps
- Reuses the app's saved vibration pattern, intensity, and pulse duration

## Test Command

You can test the app hook manually over `adb`:

```sh
adb shell am start -a de.irmo.bluebreath.action.START_ASSIST_TIMER -n de.irmo.bluebreath/.MainActivity
```

## Notes

- Screen-off volume long-press detection is device-dependent and can be unreliable without media playback.
- The BlueBreath app already contains the intent handler, so no additional app changes are needed for the Tasker setup.
