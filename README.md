# Inventory Tracker

A fully offline-first Android inventory management app built with Kotlin and Jetpack Compose. Track products, scan barcodes with the device camera, adjust stock, and review your full audit history — no account, no backend, no internet required.

![minSdk](https://img.shields.io/badge/minSdk-26-brightgreen) ![targetSdk](https://img.shields.io/badge/targetSdk-34-brightgreen) ![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-blue)

## Features

- **Barcode scanning** — On-device ML Kit scanner (CameraX) for EAN-13, UPC-A, QR, Code 128, and Code 39. No network needed.
- **Fast inventory adjustments** — One-tap `+1/+5/+10` and `-1/-5/-10` chips plus "set exact quantity". Optionally attach a reason.
- **Dashboard** — Category cards with product counts, low-stock alerts, and recent activity.
- **Products** — Search, filter by category, and sort by name, quantity, or most recently updated. Add products directly with the **＋ Add product** button.
- **Full history** — Every adjustment is logged with before/after quantities, change type, reason, and timestamp. Filter by time range (today / 7 days / 30 days) and by product.
- **Categories** — Create, edit, delete, and color-code categories. Six sensible defaults are pre-seeded.
- **Offline-first** — All data lives in a local Room database (SQLite). Works on a plane, in a basement, anywhere.
- **Multisensory scan feedback** — Vibration + beep on every successful scan.
- **Manual entry fallback** — If the camera is denied (or unavailable), type the barcode and pick its format.
- **Modern Material 3 UI** — Dynamic color on Android 12+, Jetpack Compose throughout.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0.20 |
| UI | Jetpack Compose, Material 3 (BOM 2024.09.03), Navigation Compose |
| Architecture | MVVM + Clean-ish layers (domain / data / ui), unidirectional data flow |
| DI | Hilt 2.51.1 |
| Persistence | Room 2.6.1 (SQLite), schema exported to `app/schemas` |
| Camera | CameraX 1.3.4 |
| Barcode | Google ML Kit barcode-scanning 17.3.0 (on-device) |
| Async | Kotlin Coroutines + Flow |
| Build | Gradle 8.7, AGP 8.5.2, KSP, Java 17, minify (R8) on release |
| Testing | JUnit 5 (Jupiter), MockK, Turbine, Truth, Compose UI tests |

## Project Structure

```
app/src/main/java/com/inventorytracker/app/
├── InventoryTrackerApplication.kt   # @HiltAndroidApp entry point
├── MainActivity.kt                  # Single activity, bottom nav shell
├── data/
│   ├── local/                       # Room entities, DAOs, database, converters
│   │   ├── AppDatabase.kt
│   │   ├── PrepopulateCallback.kt   # Seeds 6 default categories
│   │   ├── entity/                  # CategoryEntity, ProductEntity, InventoryLogEntity
│   │   └── dao/                     # CategoryDao, ProductDao, InventoryLogDao
│   ├── mapper/                      # Entity <-> domain model mappers
│   └── repository/                  # Repository implementations
├── domain/
│   ├── model/                       # Category, Product, InventoryLog, ChangeType, BarcodeFormat
│   ├── repository/                  # Repository interfaces
│   └── usecase/                     # All business logic (adjust, set, CRUD, lookups, filters)
├── scanner/
│   ├── BarcodeAnalyzer.kt           # CameraX + ML Kit analyzer (~15 FPS, mutex-guarded)
│   ├── BarcodeFeedback.kt           # Vibration + beep
│   └── BarcodeResult.kt
├── di/                              # Hilt modules (database, repositories)
└── ui/
    ├── navigation/                  # Routes + AppNavHost
    ├── theme/                       # Material 3 theme (dynamic color)
    ├── components/                  # Shared composables + formatting helpers
    ├── home/                        # Dashboard screen + ViewModel
    ├── scanner/                     # Camera screen + ViewModel
    ├── productlist/                 # Products list + ViewModel
    ├── productdetail/               # Product detail + adjust + history
    ├── addeditproduct/              # Add / edit product form
    ├── categories/                  # Category management
    └── history/                     # Inventory history with filters
```

## Prerequisites

- **JDK 17** (Android Studio ships with one; any 17+ works)
- **Android SDK** — platform `android-34` and build-tools `34.0.0` (Android Studio auto-installs these)
- **Android Studio** (recommended) or the command-line Gradle wrapper

The Gradle wrapper downloads Gradle 8.7 automatically on first run.

## Build & Run

1. Open the project in Android Studio (or run from a terminal).
2. Make sure `local.properties` contains your SDK path, e.g.:
   ```properties
   sdk.dir=C:\\Users\\You\\AppData\\Local\\Android\\Sdk
   ```
   (Android Studio creates this automatically; `ANDROID_HOME` also works.)
3. Run the app:
   ```bash
   ./gradlew.bat installDebug        # Windows
   ./gradlew installDebug            # macOS / Linux
   ```
   Or press **Run ▶** in Android Studio with a device/emulator selected.

## Testing

```bash
./gradlew.bat testDebugUnitTest      # 26 JVM unit tests (JUnit 5)
./gradlew.bat lintDebug              # Android Lint
./gradlew.bat connectedDebugAndroidTest  # instrumented tests (device/emulator required)
```

## Building a Release

```bash
./gradlew.bat assembleRelease        # minified APK (needs a signing config)
./gradlew.bat bundleRelease          # Android App Bundle for Play Store
```

Release builds are already configured with R8 minification + resource shrinking. The app currently has **no signing configuration** — see [DEPLOYMENT.md](./DEPLOYMENT.md) for how to generate a keystore, wire up signing, and ship to the Play Store or sideload.

## Documentation

- **[DEPLOYMENT.md](./DEPLOYMENT.md)** — Complete end-to-end deployment guide: signing keys, release builds, Play Store submission, sideloading, CI/CD, versioning, and troubleshooting.
- **[USER_GUIDE.md](./USER_GUIDE.md)** — End-user manual: how to scan, add products, adjust stock, manage categories, and read the history.
