# DocIDV - IDnow

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3-blue.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-24%2B-blueviolet.svg)](https://developer.android.com/tools/releases/platforms)

## Table of Contents

- [About](#about)
- [Key Features](#key-features)
- [Requirements](#requirements)
  - [Compatibility Matrix](#compatibility-matrix)
- [Modules Overview](#modules-overview)
  - [Dependency Graph](#dependency-graph)
- [Installation](#installation)
  - [Repository Setup](#repository-setup)
  - [Dependencies Configuration](#dependencies-configuration)
- [Integration](#integration)
  - [Initializing and starting the SDK](#initializing-and-starting-the-sdk)
  - [Handling results and errors](#handling-results-and-errors)
- [Sample App](#sample-app)
- [Integrate the NFC Dependency](#integrate-the-nfc-dependency)
- [Additional Features](#additional-features)
  - [Dark Mode & Theming](#dark-mode--theming)
  - [Localization](#localization)
- [Accessibility](#accessibility)
- [Support and more information](#support-and-more-information)
- [License](#license)

## About

Welcome to the IDnow DocIDV project. This repository is the official showcase for the DocIDV
Android SDK, which enables you to capture identity documents using the device's camera,
perform several security checks, and offer various other features.

The DocIDV SDK incorporates the IDnow platform into your Android app. Since version **1.6.0**
the SDK is **modular**: it is split into independent modules so you can include only the
features you need. This repository contains a working [sample app](#sample-app) that
demonstrates the smallest possible integration.

This guide provides a quick overview of how to install, set up, and integrate the SDK. For the
complete integration guide, customization options, and API reference, see the official
documentation at **[doc.idnow.de/sdk/android/overview](https://doc.idnow.de/sdk/android/overview/)**.

## Key Features

* **OCR**: Identity document capture.
* **OTP**: Phone number verification.
* **Liveness**: Biometric liveness check for a secure flow.
* **NFC**: NFC chip scanning for e-documents.
* **eID**: German electronic identity (eID) chip reading via Governikus.
* And many other features!

## Requirements

* **minSdkVersion:** 24 (Android 7 Nougat) — 28 (Android 9 Pie) when using the eID module.
* **compileSdkVersion:** 36 (Android 16).
* **targetSdkVersion:** 36 (Android 16).
* **Not supported:** devices and emulators based on the x86 architecture.

### Compatibility Matrix

Please refer to the following link to find information about current versions, compatibility,
end-of-support (EOS) and end-of-life (EOL) dates pertaining to our products:
[IDnow Compatibility Matrix: Browser & OS Compatibility guide](https://www.idnow.io/developers/compatibility-overview/).

## Modules Overview

The SDK is composed of the following modules:

| Module | Artifact | Required | Min SDK | Description |
|---|---|---|---|---|
| **Core** | `io.idnow.docidv:core` | ✅ Required | 24 | Base module, required by all other modules. |
| **AI** | `io.idnow.docidv:ai` | ✅ Required\* | 24 | Document capture, biometric liveness and identity verification. Corresponds to the existing SDK functionality. |
| **eID Governikus** | `io.idnow.docidv:eid-governikus` | ⚪️ Optional | 28 | German eID chip reading via Governikus. |

> \*In this version, **Core + AI** are both required as a minimum setup. In a future release,
> AI will become optional as well.

> **Note:** The eID Governikus module raises the minimum SDK level to **28** (Android 9 Pie)
> due to a restriction from the Governikus SDK.

### Dependency Graph

```
┌──────────────────────────┐
│          Core            │  ← Required by all modules
└──────┬──────────┬────────┘
       │          │
       ▼          ▼
┌──────────┐  ┌───────────────────┐
│    AI    │  │  eID Governikus   │
│(required)│  │    (optional)     │
└──────────┘  │ depends on Core   │
              │      + AI         │
              └───────────────────┘
```

## Installation

### Repository Setup

In your project's `settings.gradle` (KTS), besides any other repositories you already have,
include this maven repository:

```kotlin
repositories {
    google()
    mavenCentral()

    // Add this repo containing the DocIDV library.
    maven {
        url = uri("https://raw.githubusercontent.com/idnow/idnow-android-sdk/main")
    }
}
```

### Dependencies Configuration

The DocIDV SDK provides a **BOM** (Bill of Materials) to simplify dependency management. By
importing the BOM, you no longer need to specify individual module versions — they are all
aligned automatically.

In `libs.versions.toml`:

```toml
[versions]
docidv = "1.15.0"

[libraries]
idnow-docidv-bom = { group = "io.idnow.docidv", name = "bom", version.ref = "docidv" }
idnow-docidv-core = { group = "io.idnow.docidv", name = "core" }
idnow-docidv-ai = { group = "io.idnow.docidv", name = "ai" }
idnow-docidv-eid-governikus = { group = "io.idnow.docidv", name = "eid-governikus" }
```

In your app's `build.gradle.kts`:

```kotlin
dependencies {
    // Import the BOM
    implementation(platform(libs.idnow.docidv.bom))

    // No need to specify versions — managed by the BOM
    implementation(libs.idnow.docidv.core)
    implementation(libs.idnow.docidv.ai)

    // Optional - German eID (raises minSdk to 28)
    implementation(libs.idnow.docidv.eid.governikus)
}
```

Alternatively, without the version catalog:

```groovy
dependencies {
    // Import the BOM
    implementation platform('io.idnow.docidv:bom:1.12.0')

    // No need to specify versions
    implementation 'io.idnow.docidv:core'
    implementation 'io.idnow.docidv:ai'

    // Optional - German eID (raises minSdk to 28)
    implementation 'io.idnow.docidv:eid-governikus'
}
```

> **Note:** The old `io.idnow.docidv.android.sdk:docidv` dependency (bundling both `ai` and
> `core`) still works for now, but it will be removed in a future release. New integrations
> should use `core` + `ai`.

📥 DocIDV is now imported into your project.

## Integration

### Initializing and starting the SDK

Initialize the SDK once, then start an identification with a token generated by your backend:

```kotlin
/**
 * Initialize the IDnow SDK.
 */
private fun initSDK() {
    val idnowConfig = IDnowDocIDVConfig.Builder()
        .withLanguage("en")   // optional: set a specific ISO 639-1 language code
        .withHttpLogging(true) // optional: enable HTTP logging for debugging
        .build()
    IDnowDocIDV.getInstance().initialize(this, idnowConfig)
}

/**
 * Call this method once your backend has generated the identToken.
 * Pass an IDnowDocIDV.ResultListener to receive the outcome.
 */
private fun startIdentification(identToken: String) {
    IDnowDocIDV.getInstance().start(identToken, object : IDnowDocIDV.ResultListener {
        override fun onIdentResult(iDnowDocIDVResult: IDnowDocIDVResult) {
            when (iDnowDocIDVResult.resultType) {
                IDnowDocIDVResult.ResultType.FINISHED -> {
                    // Ident completed successfully
                    handleIdentificationSuccess()
                }
                IDnowDocIDVResult.ResultType.CANCELLED -> {
                    // Ident was cancelled by the user
                    handleIdentificationCancelled(reason = iDnowDocIDVResult.statusCode)
                }
                IDnowDocIDVResult.ResultType.ERROR -> {
                    // Ident ended with an error
                    handleIdentificationError(errorCode = iDnowDocIDVResult.statusCode)
                }
            }
        }
    })
}
```

👏 You are now ready to integrate the DocIDV SDK.

### Handling results and errors

An identification ends with one of three result types: `FINISHED`, `CANCELLED`, or `ERROR`.
In case of `ERROR`, `IDnowDocIDVResult.getStatusCode()` returns one of the following codes:

| Code | Meaning |
|---|---|
| `E100` | Ident code syntax incorrect |
| `E101` | Ident code not found |
| `E102` | Ident code expired |
| `E103` | Ident code already completed |
| `E130` | Get ident resources failed; invalid response |
| `E131` | Get ident resources failed; server reachability |
| `E150` | Start ident failed; invalid response |
| `E151` | Start ident failed; server reachability |
| `E152` | Start ident failed; missing session key |
| `E154` | Start ident failed; documents are missing and upload is disabled |
| `E160` | Process force closed |
| `E170` | Socket connection force closed |
| `E171` | Process force closed |
| `E180` | Missing application context |

#### How to deal with errors

* **E102** — Create another ident and restart the process with the new ident code.
* **E103** — Show a screen informing the user that they have submitted all required
  information and should wait for the final result.
* **E170** — Notify the user that the ident timed out or was started on a different device,
  and ask them to try again.
* **E180** — Alerts the host app that the context has been lost (OS restarted/killed the SDK
  process).
* **All other error codes** — Show a generic error and ask the user to restart the process.

🎉 That's it! DocIDV can now be launched from your host app.

## Sample App

A minimal sample app is included in this repository. It shows the smallest possible
integration: enter an identification token, tap **Start identification**, and handle the
success / error result.

Clone and open the project in Android Studio:

```bash
git clone https://github.com/idnow/docidv-sdk-android.git
```

To run it:

1. Open the project in Android Studio and let Gradle sync.
2. Select a **real device** as the run destination (the camera and NFC are not available on
   x86 emulators, which are unsupported).
3. Build and run, enter a valid identification token, and tap **Start identification**.

The relevant integration code lives in:

* [`StartFragment.kt`](./app/src/main/java/io/idnow/docidv/sample/start/StartFragment.kt) —
  initializes the SDK, starts the identification, and maps the result / error codes.
* [`HomeFragment.kt`](./app/src/main/java/io/idnow/docidv/sample/home/HomeFragment.kt) —
  collects the identification token from the user.

> This sample does not cover the NFC feature, except German eID.

## Integrate the NFC Dependency

The SDK also supports NFC scanning to read data from the chip on e-documents. It is available
in two variants — with and without the NFC feature — providing flexibility for different use
cases. We recommend using the NFC feature for the best security, fraud prevention, and user
experience.

To use the NFC feature, reach out to your IDnow contact or IDnow Support
([support@idnow.io](mailto:support@idnow.io)) to obtain the required NFC dependencies. After
your request, you will receive an archive to integrate into your application, compatible with
the corresponding SDK version and onwards. The archive contains a detailed instructions file
and the `.aar` files for the NFC dependency.

**Overview of the NFC integration:**

1. Copy all the `.aar` files from the archive's `libs` folder into your app's `libs` folder.

2. Compile the `libs` folder — in your `app/build.gradle`, add:

   ```groovy
   implementation fileTree(include: ['*.aar'], dir: 'libs')
   // or, if you also have jar files:
   // implementation fileTree(include: ['*.jar', '*.aar'], dir: 'libs')
   ```

3. In your project level `build.gradle`, make sure to declare the `flatDir` repository:

   ```groovy
   repositories {
       //...
       flatDir {
           dirs 'libs'
       }
       //...
   }
   ```

4. The AAR files require several transitive dependencies, which you need to add manually in
   your `app/build.gradle`:

   ```groovy
   // ReadID Dependencies
   // iddoc connector
   implementation 'com.google.code.gson:gson:2.11.0'
   implementation 'androidx.annotation:annotation:1.5.0'
   implementation 'org.bouncycastle:bcpkix-jdk18on:1.78.1'
   // readid mrz
   implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.8.6'
   implementation('com.google.android.gms:play-services-tflite-java:16.1.0') {
       exclude group: "org.tensorflow"
   }
   // readid core
   implementation 'org.jetbrains.kotlin:kotlin-stdlib:1.9.24'
   implementation 'net.sf.scuba:scuba-sc-android:0.0.26'
   implementation 'net.sf.scuba:scuba-smartcards:0.0.20'
   implementation 'edu.ucar:jj2000:5.2'
   implementation("org.ejbca.cvc:cert-cvc:1.4.13") {
       exclude group: 'org.bouncycastle', module: 'bcprov-jdk15on'
   }
   implementation 'org.jmrtd:jmrtd:0.7.42'
   // readid ui core
   implementation 'androidx.fragment:fragment:1.8.1'
   implementation 'androidx.preference:preference-ktx:1.2.1'
   implementation 'com.google.android.material:material:1.12.0'
   implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
   implementation 'androidx.databinding:viewbinding:8.5.2'
   // viz capture
   implementation "com.google.mlkit:barcode-scanning:17.3.0"
   implementation "com.google.mlkit:face-detection:16.1.7"
   ```

5. Add the following ProGuard rules in your application's `proguard-rules.pro` file:

   ```proguard
   # ReadID
   -keep,includedescriptorclasses class com.readid.** { *; }
   -keep enum nl.innovalor.logging.data.** { *; }
   -keep enum nl.innovalor.logging.data.**$** { *; }
   -keepclassmembers class nl.innovalor.logging.data.** { <fields>; }
   -keep,includedescriptorclasses class net.sf.scuba.smartcards.IsoDepCardService { public <init>(***); }
   -keep,includedescriptorclasses class nl.innovalor.cert.** { public <init>(***); }
   -keep,includedescriptorclasses class org.bouncycastle.** { *; }
   -keepclassmembers class * extends net.sf.scuba.data.Country { *; }
   -keep,includedescriptorclasses class nl.innovalor.mrtd.model.** { private <fields>; <methods>; }
   -dontwarn com.google.gson.**
   -dontwarn javax.naming.**
   -dontwarn javax.naming.directory.*
   -dontwarn jj2000.disp.*
   -dontwarn jj2000.j2k.decoder.*
   -dontwarn org.jmrtd.imageio.*
   -dontwarn org.jmrtd.jj2000.**
   -dontwarn org.jnbis.imageio.*
   ```

## Additional Features

### Dark Mode & Theming

Each screen of the SDK natively supports dark mode and automatically adopts your app's
appearance settings (light, dark, or system default). Configurable design themes and custom
Lottie animations are also supported to tailor the experience to your brand.

### Localization

Using `withLanguage("lang_code")` you can configure the SDK to use a specific language. The
following ISO 639-1 language codes are currently supported:

bg (Bulgarian), cs (Czech), da (Danish), de (German), el (Greek), en (English), es (Spanish),
et (Estonian), fi (Finnish), fr (French), hr (Croatian), hu (Hungarian), it (Italian),
ja (Japanese), ka (Georgian), ko (Korean), lt (Lithuanian), lv (Latvian), nb (Norwegian),
nl (Dutch), pl (Polish), pt (Portuguese), ro (Romanian), ru (Russian), sk (Slovak),
sl (Slovenian), sr (Serbian), sv (Swedish), tr (Turkish), zh (Chinese).

> **Best Practice:** For an optimal user experience, allow the SDK to use the device language
> instead of setting a specific `lang_code`.

> **Language Limitations:** Some components that rely on 3rd party technologies, such as
> Biometric Liveness or NFC scanning, do not support dynamic localization and use the device
> language.

## Accessibility

DocIDV is built with accessibility in mind, following Android accessibility guidelines and
working toward WCAG 2.1 compliance:

* **TalkBack & screen readers:** meaningful labels, logical focus order, custom actions, and
  clear announcements for component states (error, selected, disabled, progress).
* **Adjustable text size and spacing:** honors the user's system preferences.
* **Color contrast:** enhanced contrast for better readability, with visible and consistent
  focus indicators.
* **Content descriptions:** proper descriptions for images and icons.

## Support and more information

* 📚 **Full documentation:** [doc.idnow.de/sdk/android/overview](https://doc.idnow.de/sdk/android/overview/)
* 🛟 **Support portal:** [support.idnow.io](https://support.idnow.io/s/) — for technical
  questions or to raise an issue. You can also reach out to your account manager.
* 📝 See the [CHANGELOG](./CHANGELOG.md) for release notes.

## License

Use of the DocIDV SDK is governed by the IDnow SDK License Agreement. See
[LICENSE.txt](./LICENSE.txt) for the full terms.

Copyright © 2026 IDnow GmbH. All rights reserved.
