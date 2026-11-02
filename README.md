# emulator ad 🎬

A modern, high-performance Android Video Player built with a sleek **YouTube-inspired UI/UX** for playing all your local offline videos.

---

## ✨ Features

- 🎥 **YouTube-Style Feed & Player**:
  - Clean dark-mode YouTube interface.
  - Video feed displaying all local videos with auto-generated high-res thumbnails, titles, duration badges, and metadata.
  - Real-time search bar to filter videos by name or folder.
  - Filter chips (All, Recent, Large Files, Folders).
- ⚡ **All Video Formats Supported**:
  - Full support for `.mp4`, `.mkv`, `.webm`, `.avi`, `.mov`, `.flv`, `.wmv`, `.3gp`, `.ts`, `.m4v`, `.vob`, `.ogv`, and more.
  - Deep scanner scanning MediaStore Video, MediaStore Files, and internal/external storage folders (ideal for BlueStacks and emulators).
  - Open any video file directly from third-party file managers or downloads.
- ⏩ **Comprehensive Playback Speeds**:
  - YouTube-style playback speed selector:
    - **1x (Normal)**
    - **1.25x**
    - **1.5x**
    - **1.7x**
    - **2x**
    - **2.25x**
    - **2.5x**
    - **3x**
  - Instant hardware-accelerated speed adjustment with auto pitch preservation.
- 📺 **Rich Playback Controls & Fullscreen**:
  - Immersive full-screen and landscape mode with auto/manual rotation.
  - Complete gesture & touch controls: double-tap left to rewind 10s, double-tap right to forward 10s.
  - Next/Previous video navigation.
  - Precise YouTube red scrubber bar.
- 🖼️ **Picture-in-Picture (PiP)**:
  - Floating background PiP window to watch videos while using other apps.
- 💬 **Interactive Engagement Simulation**:
  - YouTube-style like/dislike buttons, share, and subscribe buttons.
  - Interactive local comments section.
  - Related videos recommendation list beneath the active video.

---

## 🛠️ GitHub Actions Build & Download APK

Every push to this repository automatically builds the latest APK using **GitHub Actions**.

### How to download the APK:
1. Navigate to the **[Actions Tab](https://github.com/Sanjuantony2255/MediaPlayer/actions)** on GitHub.
2. Click on the latest workflow run: **Build Android APK**.
3. Scroll down to the **Artifacts** section at the bottom of the summary page.
4. Download **`emulator-ad-Debug`** (or **`emulator-ad-Release-Unsigned`**), unzip it, and install `app-debug.apk` directly onto your Android device or emulator!

---

## 🏗️ Building Locally

1. Clone the repository:
   ```bash
   git clone https://github.com/Sanjuantony2255/MediaPlayer.git
   cd MediaPlayer
   ```
2. Build debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
3. The APK will be generated at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 📱 Requirements
- **Minimum SDK**: Android 8.0 (API level 26)
- **Target SDK**: Android 14 (API level 34)
- **Architecture**: Java 17 + Android Gradle Plugin 8.1.4
