# WallShift

An Android app that automatically applies high-quality wallpapers based on user-selected categories and schedules. Fresh, curated wallpapers without manual browsing.

## Features

- **Category Selection** -- Choose from Nature, Cars, Minimal, Abstract, Space, Anime, Architecture, Dark, Cityscapes, and Animals
- **Smart Selection** -- Scores images by resolution fit, aspect ratio, popularity, and freshness to pick the best wallpaper for your device
- **Scheduled Changes** -- Set wallpapers to change every 30 min, 1 hour, 6 hours, 12 hours, or daily
- **Wallpaper Target** -- Apply to home screen, lock screen, or both
- **Duplicate Prevention** -- Tracks applied wallpapers so you never see the same one twice
- **Image Caching** -- Downloaded images are cached locally (configurable 100 MB limit with LRU eviction)
- **Save to Device** -- Save any wallpaper to your Pictures folder
- **Survives Reboots** -- Background scheduling resumes automatically after device restart
- **Multiple Sources** -- Fetches from Unsplash and Pexels APIs for maximum variety

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material3 (Material You dynamic color on Android 12+)
- **Architecture**: MVVM + Repository + Use Cases
- **DI**: Hilt
- **Networking**: Retrofit + OkHttp + Moshi
- **Database**: Room
- **Scheduling**: WorkManager
- **Image Loading**: Coil
- **Preferences**: DataStore

## Project Structure

```
com.wallshift.app/
  di/                  # Hilt dependency injection modules
  data/
    api/               # Retrofit services & DTOs (Unsplash, Pexels)
    db/                # Room entities, DAOs, database
    repository/        # Repository implementations & DataStore
    cache/             # LRU image cache manager
  domain/
    model/             # Domain models (WallpaperImage, etc.)
    usecase/           # Business logic use cases
    repository/        # Repository interfaces
  ui/
    onboarding/        # First-launch category & schedule setup
    home/              # Main wallpaper preview screen
    settings/          # App settings screen
    components/        # Shared composables
    theme/             # Material3 theming
  worker/              # WorkManager background workers
  receiver/            # BroadcastReceivers (boot)
  util/                # Constants & extensions
```

## Setup

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 35

### API Keys

1. Copy `local.properties.template` to `local.properties`
2. Get an API key from [Unsplash Developers](https://unsplash.com/developers)
3. Get an API key from [Pexels API](https://www.pexels.com/api/)
4. Add your keys:

```properties
UNSPLASH_API_KEY=your_unsplash_key
PEXELS_API_KEY=your_pexels_key
```

### Build & Run

```bash
./gradlew assembleDebug
```

Or open the project in Android Studio and click Run.

## Permissions

| Permission | Purpose |
|---|---|
| `SET_WALLPAPER` | Apply wallpapers to home/lock screen |
| `INTERNET` | Fetch images from Unsplash/Pexels APIs |
| `RECEIVE_BOOT_COMPLETED` | Re-schedule wallpaper changes after reboot |

## Attribution

Images are provided by [Unsplash](https://unsplash.com) and [Pexels](https://www.pexels.com). All photos are licensed for free use under their respective licenses.

## License

See [LICENSE](LICENSE) for details.
