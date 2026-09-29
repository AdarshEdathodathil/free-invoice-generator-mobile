# iOS host

The `shared` module generates a static framework named `Shared` for iOS.

The SwiftUI host source is included in this directory:

- `FreeInvoiceGeneratorApp.swift` starts the native application lifecycle.
- `ComposeRootView.swift` hosts `MainViewController()` from the shared framework.

On macOS, create an Xcode iOS App project in this directory, add these Swift
files to its target, and link the `Shared` framework through the Compose
Multiplatform Xcode integration. Xcode must be used for framework linking,
code signing, simulator execution, and App Store archives.
