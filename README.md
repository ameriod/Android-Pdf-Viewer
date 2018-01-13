Android Pdf Viewer
==================

A simple library that wraps the Android's `PdfRenderer`, `ViewPager` to swipe between pages and the [https://github.com/chrisbanes/PhotoView](PhotoViewer) for pinch and zoom support.

Dependency
----------
Add this in your root build.gradle file (not your module build.gradle file):

```
allprojects {
	repositories {
        maven { url "https://jitpack.io" }
    }
}
```

Then, add the library to your module build.gradle

```
dependencies {
    implementation 'com.github.ameriod:Android-Pdf-Viewer:1.0.0'
}
```

Usage
-----

```
<me.ameriod.lib.pdfviewer.PdfViewerView
    android:id="@+id/pdfViewer"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

Then call one of the `setPdf` methods to display the PDF.
