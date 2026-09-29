import SwiftUI
import UIKit
import Shared

/// Hosts the shared Compose UI inside the native iOS app lifecycle.
struct ComposeRootView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) { }
}
