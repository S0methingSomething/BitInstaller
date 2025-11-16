# BitInstaller

An Android app that installs configuration files to other apps using Shizuku, with configuration driven by TOML files.

## Features

- **Config-driven**: All app configurations stored in `assets/apps.toml`
- **GitHub Integration**: Automatically fetches releases from GitHub API
- **Shizuku Integration**: Uses Shizuku for root-level file operations
- **SHA-256 Verification**: Displays file hash for security verification
- **MVVM Architecture**: Clean architecture with ViewModel and Coroutines
- **Material Design**: Modern UI with Material Components

## Requirements

- Android 7.0 (API 24) or higher
- Shizuku app installed and running
- Target app must be installed

## Project Structure

```
app/
├── src/main/
│   ├── assets/
│   │   └── apps.toml              # App configuration
│   ├── java/com/community/bitinstaller/
│   │   ├── adapter/
│   │   │   └── AppListAdapter.kt  # RecyclerView adapter
│   │   ├── models/
│   │   │   └── Models.kt          # Data models
│   │   ├── network/
│   │   │   └── GitHubApiService.kt # GitHub API client
│   │   ├── utils/
│   │   │   ├── ConfigLoader.kt    # TOML parser
│   │   │   ├── FileDownloader.kt  # Download with SHA-256
│   │   │   └── ShizukuHelper.kt   # Shizuku operations
│   │   ├── viewmodel/
│   │   │   └── MainViewModel.kt   # Business logic
│   │   ├── MainActivity.kt        # Main screen
│   │   └── DownloadActivity.kt    # Download screen
│   └── res/
│       └── layout/                # UI layouts
```

## Adding New Apps

Edit `app/src/main/assets/apps.toml`:

```toml
[[apps]]
package_name = "com.example.app"
app_name = "Example App"
target_path = "files/config"

[apps.github]
release_tag = "v1.0.0"
asset_name = "config.dat"
```

No code changes required!

## Build

```bash
./gradlew assembleDebug
```

## Dependencies

- OkHttp - Network requests
- Gson - JSON parsing
- tomlkt - TOML parsing
- Kotlin Coroutines - Async operations
- Shizuku API - Root file operations
- AndroidX - Modern Android components

## License

This project is for educational purposes.
