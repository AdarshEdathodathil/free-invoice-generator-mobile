#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
APP_PATH="$PROJECT_ROOT/build/ios/Build/Products/Debug-iphonesimulator/Free Invoice Generator.app"
BUNDLE_ID="com.tociva.freeinvoicegenerator"
REQUESTED_DEVICE="${1:-${IOS_SIMULATOR:-}}"

if [[ "$(uname -s)" != "Darwin" ]]; then
    echo "The iOS Simulator is available only on macOS." >&2
    exit 1
fi

if ! command -v xcrun >/dev/null 2>&1; then
    echo "xcrun was not found. Install Xcode and select it with xcode-select." >&2
    exit 1
fi

echo "Building the iOS app. Unchanged sources are skipped."
bash "$SCRIPT_DIR/build-ios.sh"

if [[ ! -d "$APP_PATH" ]]; then
    echo "iOS app bundle was not produced at: $APP_PATH" >&2
    exit 1
fi

device_udid() {
    local device_filter="$1"

    if [[ -n "$device_filter" ]]; then
        xcrun simctl list devices available \
            | grep -F "$device_filter" \
            | sed -nE 's/.*\(([0-9A-Fa-f-]{36})\) \((Booted|Shutdown)\).*/\1/p' \
            | sed -n '1p' \
            || true
    else
        xcrun simctl list devices available \
            | sed -nE '/iPhone/ s/.*\(([0-9A-Fa-f-]{36})\) \((Booted|Shutdown)\).*/\1/p' \
            | sed -n '1p'
    fi
}

if [[ -n "$REQUESTED_DEVICE" ]]; then
    UDID="$(device_udid "$REQUESTED_DEVICE")"
    if [[ -z "$UDID" ]]; then
        echo "No available iPhone Simulator matched: $REQUESTED_DEVICE" >&2
        exit 1
    fi
else
    UDID="$(xcrun simctl list devices booted | sed -nE '/iPhone/ s/.*\(([0-9A-Fa-f-]{36})\) \(Booted\).*/\1/p' | sed -n '1p')"
    [[ -n "$UDID" ]] || UDID="$(device_udid "")"
fi

if [[ -z "$UDID" ]]; then
    echo "No iPhone Simulator is installed. Add one from Xcode's Platforms settings." >&2
    exit 1
fi

xcrun simctl boot "$UDID" 2>/dev/null || true
open -a Simulator
xcrun simctl bootstatus "$UDID" -b
xcrun simctl install "$UDID" "$APP_PATH"
xcrun simctl launch "$UDID" "$BUNDLE_ID"

echo "Opened Free Invoice Generator on iOS Simulator $UDID."
