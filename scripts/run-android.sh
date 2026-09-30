#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"
PACKAGE_NAME="com.tociva.freeinvoicegenerator"
MAIN_ACTIVITY="$PACKAGE_NAME/com.example.freeinvoicegeneratorbydaybookcloud.MainActivity"
REQUESTED_AVD="${1:-${ANDROID_AVD:-}}"
USER_HOME="${HOME:?HOME must be set}"
ANDROID_SDK="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$USER_HOME/Library/Android/sdk}}"

resolve_tool() {
    local tool_name="$1"
    local sdk_path="$2"

    if command -v "$tool_name" >/dev/null 2>&1; then
        command -v "$tool_name"
    elif [[ -x "$sdk_path" ]]; then
        printf '%s\n' "$sdk_path"
    else
        echo "$tool_name was not found. Install Android SDK Platform-Tools and Emulator." >&2
        exit 1
    fi
}

ADB="$(resolve_tool adb "$ANDROID_SDK/platform-tools/adb")"
EMULATOR="$(resolve_tool emulator "$ANDROID_SDK/emulator/emulator")"

echo "Building the Android app. Unchanged sources are skipped."
bash "$SCRIPT_DIR/build-android.sh"

if [[ ! -f "$APK_PATH" ]]; then
    echo "Android APK was not produced at: $APK_PATH" >&2
    exit 1
fi

find_running_emulator() {
    "$ADB" devices | awk '$1 ~ /^emulator-/ && $2 == "device" { print $1; exit }'
}

SERIAL="$(find_running_emulator)"

if [[ -z "$SERIAL" ]]; then
    if [[ -z "$REQUESTED_AVD" ]]; then
        REQUESTED_AVD="$("$EMULATOR" -list-avds | sed -n '1p')"
    fi

    if [[ -z "$REQUESTED_AVD" ]]; then
        echo "No Android Virtual Device is configured. Create one in Android Studio Device Manager." >&2
        exit 1
    fi

    echo "Starting Android emulator: $REQUESTED_AVD"
    "$EMULATOR" -avd "$REQUESTED_AVD" >"${TMPDIR:-/tmp}/daybook-android-emulator.log" 2>&1 &

    for _ in $(seq 1 120); do
        SERIAL="$(find_running_emulator)"
        [[ -n "$SERIAL" ]] && break
        sleep 2
    done
fi

if [[ -z "$SERIAL" ]]; then
    echo "The Android emulator did not become available." >&2
    exit 1
fi

echo "Waiting for Android to finish booting: $SERIAL"
for _ in $(seq 1 120); do
    BOOT_COMPLETED="$("$ADB" -s "$SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)"
    [[ "$BOOT_COMPLETED" == "1" ]] && break
    sleep 2
done

if [[ "$("$ADB" -s "$SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)" != "1" ]]; then
    echo "Android did not finish booting in time." >&2
    exit 1
fi

"$ADB" -s "$SERIAL" install -r "$APK_PATH"
"$ADB" -s "$SERIAL" shell am start -n "$MAIN_ACTIVITY"

echo "Opened Free Invoice Generator on $SERIAL."
