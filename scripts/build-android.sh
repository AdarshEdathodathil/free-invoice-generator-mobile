#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_ROOT"

USER_HOME="${HOME:?HOME must be set}"
if [[ -z "${ANDROID_HOME:-}" && -z "${ANDROID_SDK_ROOT:-}" ]]; then
    DEFAULT_SDK="$USER_HOME/Library/Android/sdk"
    if [[ -d "$DEFAULT_SDK" ]]; then
        export ANDROID_HOME="$DEFAULT_SDK"
    fi
fi

bash gradlew assembleDebug

echo "Android APK: $PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"
