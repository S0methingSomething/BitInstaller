# BitInstaller

An Android app that installs configuration files to other apps using Shizuku, with configuration driven by TOML files.

## Features

- **Config-driven**: All app configurations stored in `assets/apps.toml`
- **GitHub Integration**: Automatically fetches releases from GitHub API
- **Shizuku Integration**: Uses Shizuku for root-level file operations
- **SHA-256 Verification**: Optional hash verification for security
- **MVVM Architecture**: Clean architecture with ViewModel and Coroutines
- **Material Design**: Modern UI with Material Components
- **Input Validation**: Prevents path traversal and command injection attacks

## Security

### Implemented Protections

1. **Command Injection Prevention**: Direct command execution without shell interpretation
2. **Path Traversal Protection**: Validates all paths using canonical path resolution
3. **Input Validation**: Package names, GitHub repos, and file paths are validated
4. **SHA-256 Verification**: Optional hash verification for downloaded files
5. **Automatic Cleanup**: Downloaded files are deleted after use or on error

### Security Considerations

- Requires Shizuku for privileged operations
- QUERY_ALL_PACKAGES permission used to check if target apps are installed
- All user input is validated before use
- Network timeouts prevent hanging connections
- See [SECURITY_FIXES.md](SECURITY_FIXES.md) for detailed security improvements

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
│   │   │   └── AppListAdapter.kt  # RecyclerView with DiffUtil
│   │   ├── models/
│   │   │   ├── Models.kt          # Data models
│   │   │   └── AppError.kt        # Error types
│   │   ├── network/
│   │   │   └── GitHubApiService.kt # GitHub API client
│   │   ├── utils/
│   │   │   ├── ConfigLoader.kt    # TOML parser
│   │   │   ├── FileDownloader.kt  # Download with SHA-256
│   │   │   ├── ShizukuHelper.kt   # Shizuku operations
│   │   │   ├── InputValidator.kt  # Security validation
│   │   │   ├── Constants.kt       # App constants
│   │   │   ├── Extensions.kt      # Kotlin extensions
│   │   │   ├── NetworkUtils.kt    # Network checks
│   │   │   └── StorageUtils.kt    # Disk space checks
│   │   ├── viewmodel/
│   │   │   └── MainViewModel.kt   # Business logic
│   │   ├── MainActivity.kt        # Main screen
│   │   └── DownloadActivity.kt    # Download screen
│   └── res/
│       ├── layout/                # UI layouts
│       └── values/
│           ├── colors.xml         # Color resources
│           └── strings.xml        # String resources
└── src/test/
    └── java/com/community/bitinstaller/
        ├── InputValidatorTest.kt  # Validation tests
        ├── ConfigLoaderTest.kt    # Config parsing tests
        └── FileDownloaderTest.kt  # Download tests
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
expected_sha256 = "abc123..."  # Optional but recommended
```

No code changes required!

## Build

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires keystore credentials in environment)
export KEYSTORE_PASSWORD=your_password
export KEY_ALIAS=your_alias
export KEY_PASSWORD=your_key_password
./gradlew assembleRelease

# Run tests
./gradlew test

# Run code quality checks
./gradlew checkCode
```

## Architecture

### MVVM Pattern
- **View**: Activities observe ViewModel state via StateFlow
- **ViewModel**: Business logic and state management
- **Model**: Data classes and repository pattern (planned)

### Key Components
- **InputValidator**: Validates all user input for security
- **FileDownloader**: Handles downloads with progress and verification
- **ShizukuHelper**: Manages privileged file operations
- **AppListAdapter**: RecyclerView with DiffUtil for efficient updates

## Dependencies

- **OkHttp** - Network requests with timeouts
- **Gson** - JSON parsing
- **tomlkt** - TOML parsing
- **Kotlin Coroutines** - Async operations
- **Shizuku API** - Root file operations
- **AndroidX** - Modern Android components
- **MockK** - Testing framework
- **MockWebServer** - Network testing

## Testing

```bash
# Run unit tests
./gradlew test

# Run with coverage
./gradlew testDebugUnitTest

# Run static analysis
./gradlew detekt
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests and code quality checks
5. Submit a pull request

## License

This project is for educational purposes.

## Changelog

### Recent Improvements
- ✅ Fixed critical command injection vulnerability
- ✅ Added path traversal protection
- ✅ Implemented SHA-256 verification
- ✅ Added comprehensive input validation
- ✅ Replaced hardcoded colors with resources
- ✅ Added network timeouts and error handling
- ✅ Implemented DiffUtil for RecyclerView
- ✅ Added unit tests for core components
- ✅ Added KDoc documentation
- ✅ Improved error messages for users

