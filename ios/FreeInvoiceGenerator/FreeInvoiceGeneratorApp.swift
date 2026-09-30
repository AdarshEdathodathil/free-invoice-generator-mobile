import SwiftUI

@main
struct FreeInvoiceGeneratorApp: App {
    var body: some Scene {
        WindowGroup {
            ComposeRootView()
                .ignoresSafeArea(.keyboard)
        }
    }
}
