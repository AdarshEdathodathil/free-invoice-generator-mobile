package android.print;

// Android exposes these callback types but gives their constructors package access.
// Keep the access bridge here; invoice rendering remains in the application package.
public final class PdfCallbacks {
    private PdfCallbacks() {}

    public abstract static class Layout extends PrintDocumentAdapter.LayoutResultCallback {
        public Layout() {}
    }

    public abstract static class Write extends PrintDocumentAdapter.WriteResultCallback {
        public Write() {}
    }
}
