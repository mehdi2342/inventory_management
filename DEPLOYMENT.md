# Deployment Guide

This guide walks you from source code to a signed, distributable Android app — covering a signed release APK for sideloading, an Android App Bundle (AAB) for the Google Play Store, and how to set up signing keys, versioning, CI/CD, and troubleshooting.

> **Current signing status:** the project builds a minified **unsigned** release APK out of the box. Before you distribute anything, you **must** create a signing keystore and sign your release build (Section 2). A signed APK/AAB is required for all real distribution channels.

---

## Table of Contents

1. [Prerequisites & Environment](#1-prerequisites--environment)
2. [Create a Signing Keystore](#2-create-a-signing-keystore)
3. [Wire Up Gradle Signing](#3-wire-up-gradle-signing)
4. [Build the Release Artifacts](#4-build-the-release-artifacts)
   - [Signed APK (sideload)](#41-signed-apk-sideload)
   - [Android App Bundle (Play Store)](#42-android-app-bundle-play-store)
5. [Verify the Build](#5-verify-the-build)
6. [Distribute via Google Play](#6-distribute-via-google-play)
7. [Distribute via Sideloading](#7-distribute-via-sideloading)
8. [Versioning & Updates](#8-versioning--updates)
9. [Release Checklist](#9-release-checklist)
10. [CI/CD (GitHub Actions)](#10-cicd-github-actions)
11. [R8 / ProGuard Notes](#11-r8--proguard-notes)
12. [Troubleshooting](#12-troubleshooting)

---

## 1. Prerequisites & Environment

| Tool | Version | Notes |
|---|---|---|
| JDK | 17+ | Gradle toolchain requirement |
| Android SDK | platform 34, build-tools 34.0.0 | `compileSdk`/`targetSdk` are 34 |
| Gradle | 8.7 (wrapper) | Bundled — no manual install |
| Android Studio | Any recent (optional) | Handy for SDK management & signing |

### Verify the environment

```powershell
# Windows
.\gradlew.bat --version
# macOS / Linux
./gradlew --version
```

Ensure `local.properties` exists in the project root with your SDK path:

```properties
sdk.dir=C:\\Users\\You\\AppData\\Local\\Android\\Sdk
```

`ANDROID_HOME` (or `ANDROID_SDK_ROOT`) also works as an environment variable. Android Studio generates `local.properties` automatically.

### Quick sanity build

```bash
# Windows
.\gradlew.bat assembleDebug
# macOS / Linux
./gradlew assembleDebug
```

You should see `BUILD SUCCESSFUL` and an APK at `app/build/outputs/apk/debug/app-debug.apk`.

---

## 2. Create a Signing Keystore

A keystore is a password-protected file containing your signing key. **Losing it is irreversible** — you can never update the app on the Play Store with a different key. Back it up.

### 2.1 Generate the keystore (keytool)

```powershell
keytool -genkeypair -v `
  -keystore "C:\secure\keys\inventory-tracker-release.jks" `
  -alias inventorytracker `
  -keyalg RSA -keysize 2048 -validity 10000 `
  -storepass "YourStorePass123" `
  -keypass "YourKeyPass123" `
  -dname "CN=Inventory Tracker, OU=Dev, O=YourOrg, L=City, S=State, C=US"
```

On macOS/Linux replace the line-continuation backtick `` ` `` with `\`.

**What you must do:**

- Store the keystore **outside** the repository (e.g. `C:\secure\keys\`).
- Keep at least two copies (USB drive, password manager, printed backup).
- Write down the store password, key password, and alias somewhere safe.
- **Never** commit the keystore or passwords to git.

> **Validity:** 10000 days (~27 years). Play App Signing can rotate your app-signing key later if needed, so you can replace an upload key, but your upload key must stay consistent until then.

### 2.2 (Alternative) `keytool` one-liner without `-dname` prompts

If you don't want the verbose form, the tool will prompt interactively; answering the fields is fine for personal use.

---

## 3. Wire Up Gradle Signing

We keep passwords **out of the build files** by reading them from environment variables / `~/.gradle/gradle.properties`, with a `signingConfigs` block in `app/build.gradle.kts`.

### 3.1 Add the signing config

In `app/build.gradle.kts`, inside the `android { }` block, add:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("INVENTORY_KEYSTORE") ?: "C:/secure/keys/inventory-tracker-release.jks")
            storePassword = System.getenv("INVENTORY_STORE_PASSWORD") ?: System.getenv("INVENTORY_KEY_PASSWORD")
            keyAlias = "inventorytracker"
            keyPassword = System.getenv("INVENTORY_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")   // <-- add this line
        }
    }
}
```

### 3.2 Set the secrets in `~/.gradle/gradle.properties`

Put these in your **user-level** Gradle properties (`C:\Users\<you>\.gradle\gradle.properties` or `~/.gradle/gradle.properties`), not the project file, so they never enter the repo:

```properties
# C:\Users\<you>\.gradle\gradle.properties
INVENTORY_KEYSTORE=C:\\secure\\keys\\inventory-tracker-release.jks
INVENTORY_STORE_PASSWORD=YourStorePass123
INVENTORY_KEY_PASSWORD=YourKeyPass123
```

Alternatively set them as real environment variables in CI — see [Section 10](#10-cicd-github-actions).

### 3.3 Verify signing is configured

```powershell
.\gradlew.bat :app:signingReport
```

Look for `Release:` → `V1: ... V2: ...` with `✓` marks. If it says `None`, the keystore path or passwords are wrong.

---

## 4. Build the Release Artifacts

### 4.1 Signed APK (sideload)

```powershell
.\gradlew.bat assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk` (signed, minified, resource-shrunk). Size is typically ~23 MB with the bundled ML Kit barcode model.

Install on a device:

```powershell
adb install -r app\build\outputs\apk\release\app-release.apk
```

### 4.2 Android App Bundle (Play Store)

```powershell
.\gradlew.bat bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`. Google Play generates and serves optimized APKs per-device from this file — this is the **recommended** format for Play.

---

## 5. Verify the Build

### 5.1 Check the APK signature

```powershell
apksigner verify --print-certs app\build\outputs\apk\release\app-release.apk
```

(`apksigner` lives in `build-tools\34.0.0\`.) You should see your certificate SHA-256 and `v1`/`v2`/`v3` scheme support.

### 5.2 Confirm APK metadata

```powershell
aapt2 dump badging app\build\outputs\apk\release\app-release.apk
```

Checks: `package name`, `sdkVersion:'26'`, `targetSdkVersion:'34'`, and permissions `CAMERA` + `VIBRATE`.

### 5.3 Run the full quality gate

```powershell
.\gradlew.bat lintDebug testDebugUnitTest assembleRelease
```

All three must succeed: **Lint (0 errors)**, **26 unit tests**, and a signed release build.

### 5.4 Validate an AAB (optional)

Using `bundletool` (download from GitHub releases):

```powershell
bundletool get-device-spec --output=spec.json
bundletool build-apks --bundle=app-release.aab --output=app.apks --ks=... --ks-pass=... --device-spec=spec.json
```

---

## 6. Distribute via Google Play

### 6.1 Set up Play Console

1. Go to [play.google.com/console](https://play.google.com/console) and accept the Developer Agreement (one-time **$25** registration fee).
2. **Create app** → choose name ("Inventory Tracker"), default language, app type (App), free/paid.

### 6.2 Prepare the store listing (production)

Fill in the required sections:

- **App content:** Privacy policy URL, ad declaration (none), content rating questionnaire, target audience.
- **Data safety:** Declare honestly — the app **collects no data** and works fully offline; it uses the **camera** (barcode scanning) and stores everything locally on-device.
- **Production track:** upload `app-release.aab`, add release notes, and set version.

### 6.3 App signing — choose Play App Signing

When uploading your AAB, Play Console will offer **Play App Signing** (recommended):

1. It takes the upload key you generated and keeps your app-signing key in Google's custody.
2. **Export & upload the upload key certificate** from your keystore:
   ```powershell
   keytool -export -rfc -keystore inventory-tracker-release.jks -alias inventorytracker `
     -file upload_cert.pem
   ```
3. If you ever lose the upload keystore, you can request a replacement upload key via the console (involving support review).

### 6.4 Testing tracks before production

- **Internal testing** (up to 100 testers, instant) — upload AAB, add tester emails, they install via an opt-in link.
- **Closed testing** (up to 10,000 testers) — good staging step with review.
- **Open testing** — public beta via opt-in link.
- Run through the full app (scan, add, adjust, history) in the internal track before submitting to production.

### 6.5 Submit for review

Create a release from the AAB, set a release name like `1.0 (1)`, choose rollout percentage (start at 10–20% for staged rollouts), and submit. New apps typically review in hours to a few days.

---

## 7. Distribute via Sideloading

For internal teams, QA, or personal use:

1. Build the signed APK (Section 4.1).
2. Share the file directly (email, shared drive, QR download).
3. Recipients must enable "Install unknown apps" for the source (Settings → Security → Install unknown apps) on Android 8+, or the browser you downloaded from.
4. Verify the signature matches your published certificate to prove authenticity.

> Play Protect may warn on first sideload — this is normal for non-Play apps.

---

## 8. Versioning & Updates

Version fields live in `app/build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 1      // integer, must INCREASE for every Play release
    versionName = "1.0"  // human-readable, e.g. "1.1.0"
}
```

- **`versionCode`** must strictly increase on each Play Store upload.
- A common scheme: major.minor.patch → `versionCode` = `major * 10000 + minor * 100 + patch` (e.g. 1.2.0 → 10200).

**Update checklist:**
1. Bump `versionCode`/`versionName`.
2. Run `assembleRelease` + `bundleRelease`.
3. Upload the new AAB to the same track, write release notes.
4. Roll out (full or staged).

Because the app is offline-first, there is **no server-side migration** — Room handles schema migration on the device (`@Database(version = ...)`), so just ship the new APK/AAB.

---

## 9. Release Checklist

- [ ] Keystore created, backed up off-repo, passwords safe.
- [ ] `signingConfig` wired; `signingReport` shows valid V1/V2/V3.
- [ ] `versionCode`/`versionName` bumped for this release.
- [ ] `.\gradlew.bat lintDebug` → 0 errors.
- [ ] `.\gradlew.bat testDebugUnitTest` → all pass.
- [ ] `assembleRelease` produces a signed APK.
- [ ] `apksigner verify` passes and prints your cert.
- [ ] `bundleRelease` produces an AAB.
- [ ] Smoke-tested on a real device: scan, add, adjust, set, delete, history, categories.
- [ ] Tested the **no-camera** path (deny permission → manual entry).
- [ ] Play listing content: privacy policy, data safety, rating questionnaire.
- [ ] No secrets/keystore in git (`git status` clean of `.jks`, passwords).

---

## 10. CI/CD (GitHub Actions)

Example workflow `.github/workflows/release.yml` that builds and signs a release APK using GitHub Secrets:

```yaml
name: Build release

on:
  push:
    tags: ["v*"]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "17"

      - uses: actions/setup-android@v3
        with:
          sdkversion: "34.0.0"

      - name: Decode keystore
        run: |
          echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > release.jks
        env:
          KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}

      - name: Build signed release
        env:
          INVENTORY_KEYSTORE: ${{ github.workspace }}/release.jks
          INVENTORY_STORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          INVENTORY_KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew assembleRelease

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: app-release
          path: app/build/outputs/apk/release/app-release.apk

      - name: Upload AAB
        run: ./gradlew bundleRelease
      - name: Upload AAB artifact
        uses: actions/upload-artifact@v4
        with:
          name: app-release-bundle
          path: app/build/outputs/bundle/release/app-release.aab
```

**Secret setup:** GitHub repo → Settings → Secrets → Actions:
- `KEYSTORE_BASE64` — `base64 -w0 inventory-tracker-release.jks` output
- `KEYSTORE_PASSWORD`, `KEY_PASSWORD`

For Play publishing from CI, look into the **Google Play Developer API** + `gradle-play-publisher` or the `google-github-actions/upload-application` action.

---

## 11. R8 / ProGuard Notes

Release builds run R8 (minify) + resource shrinking. The existing `app/proguard-rules.pro` already keeps:

- **ML Kit barcode** classes (runtime model metadata)
- **CameraX** (reflection-based code paths)
- **Room `@Entity`** classes (reflection for schema validation)
- **Gson `@SerializedName`** fields (transitive dependency)

If you add libraries that use reflection (Retrofit, Moshi, etc.), add the appropriate keep rules. When in doubt, run:

```powershell
.\gradlew.bat :app:assembleRelease --stacktrace
```

and check `app/build/outputs/mapping/release/mapping.txt` if you need to debug missing class errors.

---

## 12. Troubleshooting

| Symptom | Fix |
|---|---|
| `sdk.dir` error / "SDK location not found" | Create `local.properties` with `sdk.dir=C:\\...\\Android\\Sdk` or set `ANDROID_HOME`. |
| `Unsupported class file major version` | Install JDK 17 and ensure `JAVA_HOME` points to it. |
| `app-release-unsigned.apk` / `isSigned=false` | Signing config not applied — re-check Section 3 and run `signingReport`. |
| `FAILURE: Build failed` on lint: `UnsafeOptInUsageError` | The scanner's experimental CameraX API usage is already annotated/suppressed; if you add new analyzer code touching `imageProxy.image`, add `@OptIn(ExperimentalGetImage::class)` (+ `@SuppressLint("UnsafeOptInUsageError")` on the method). |
| APK won't install on a device | Device must run Android **8.0 (API 26) or newer** (minSdk 26). Enable "Install unknown apps" for the installer. |
| `v2/v3 signature` rejected on old devices | Install on a newer device; V2 signing requires API 24+, which is fine given minSdk 26. |
| Play rejects AAB | Validate with `bundletool build-apks` first; make sure `versionCode` increased; complete all store-listing fields. |
| Build slow / Gradle daemon issues | The wrapper caches Gradle in `~\.gradle`. Use `--offline` if dependencies are already cached. |
| Scan not working on a device | Grant the **Camera** permission; ensure the barcode is one of EAN-13, UPC-A, QR, Code 128, Code 39; use the manual entry as a fallback. ML Kit downloads the model on first use (small, one-time). |
