# Web7 Media Player 🎬

A modern, high-performance Android Video Player built with a sleek **YouTube-inspired UI/UX** for playing your local offline videos.

![Web7 Banner](https://raw.githubusercontent.com/Sanjuantony2255/MediaPlayer/main/app/src/main/res/drawable/ic_launcher_foreground.xml)

---

## ✨ Features

- 🎥 **YouTube-Style Feed & Player**:
  - Clean dark-mode YouTube interface.
  - Video feed displaying local videos with auto-generated high-res thumbnails, titles, duration badges, and metadata.
  - Search bar to instantly filter videos by name.
  - Filter chips (All, Recent, Large Files, Videos).
- 📺 **Rich Video Playback**:
  - Immersive full-screen and landscape mode with auto/manual rotation.
  - Full player controls (Play/Pause, Rewind 10s, Forward 10s, Next/Prev video).
  - Modern YouTube red scrub bar with duration indicator.
  - Playback speed selection (0.25x, 0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 1.75x, 2.0x).
- 🖼️ **Picture-in-Picture (PiP)**:
  - Seamless background & floating PiP mode to watch videos while using other apps.
- 💬 **Interactive Engagement Simulation**:
  - YouTube-style like/dislike buttons, share, and subscribe buttons.
  - Simulated interactive comments section (add and view comments for any video).
  - Related videos recommendation list beneath the active video.
- ⚡ **Local File System Integration**:
  - High-speed device media scanner (`MediaStore`).
  - No internet connection required for playing your local videos.

---

## 🛠️ GitHub Actions Build & Download APK

Every push to this repository automatically builds the latest APK using **GitHub Actions**.

### How to download the APK:
1. Navigate to the **[Actions Tab](https://github.com/Sanjuantony2255/MediaPlayer/actions)** on GitHub.
2. Click on the latest workflow run: **Build Android APK**.
3. Scroll down to the **Artifacts** section at the bottom of the summary page.
4. Download **`Web7-Player-Debug`** (or **`Web7-Player-Release-Unsigned`**), unzip it, and install `app-debug.apk` directly onto your Android device!

---

## 🏗️ Building Locally

1. Clone the repository:
   ```bash
   git clone https://github.com/Sanjuantony2255/MediaPlayer.git
   cd MediaPlayer
   ```
2. Open with **Android Studio** (Hedgehog or newer) or build from terminal:
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
