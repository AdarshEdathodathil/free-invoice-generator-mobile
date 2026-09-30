#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
DERIVED_DATA_PATH="$PROJECT_ROOT/build/ios"
FRAMEWORK_DIR="$PROJECT_ROOT/shared/build/xcode-frameworks/Debug/iphonesimulator26.0"

if [[ "$(uname -s)" != "Darwin" ]]; then
    echo "The iOS app must be built on macOS with Xcode installed." >&2
    exit 1
fi

if ! command -v xcodebuild >/dev/null 2>&1; then
    echo "xcodebuild was not found. Install Xcode and select it with xcode-select." >&2
    exit 1
fi

echo "Building the iOS app with Xcode (Shared framework is built by the Xcode run script)."
cd "$PROJECT_ROOT"
xcodebuild \
    -project "$PROJECT_ROOT/ios/FreeInvoiceGenerator.xcodeproj" \
    -scheme FreeInvoiceGenerator \
    -configuration Debug \
    -sdk iphonesimulator \
    -destination "generic/platform=iOS Simulator" \
    -derivedDataPath "$DERIVED_DATA_PATH" \
    FRAMEWORK_SEARCH_PATHS="$PROJECT_ROOT/shared/build/bin/iosSimulatorArm64/debugFramework" \
    OTHER_LDFLAGS="-framework Shared" \
    CODE_SIGNING_ALLOWED=NO \
    build

echo "iOS app: $DERIVED_DATA_PATH/Build/Products/Debug-iphonesimulator/Free Invoice Generator.app"
