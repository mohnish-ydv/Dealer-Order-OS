# Apex DMS Native

A native Android Dealer Management System rebuilt from the previous DMS concept with a mobile-first Shoppe-inspired UX.

This is **not** a WebView/PWA wrapper. The UI is Kotlin + Jetpack Compose + Material 3.

## Core journeys
Dealer: Shop → Product → Request Price → Quotation → Accept/Revision → Order → Delivery → Repeat Order.

Staff: Commercial inbox → Review/Assign → Quote Builder → Send → Order/Fulfilment, plus Dealers, Products, Inventory, Reports, Sales, Activity and Settings.

See `APP_WORKFLOW.md` for the complete journey and `FINAL_AUDIT.md` for the release audit.

## Stack
- Kotlin 1.9.25
- Jetpack Compose
- Material 3
- Navigation Compose
- AndroidViewModel
- local JSON persistence with Gson
- minSdk 26 / targetSdk 35
- Java 17

## Build on GitHub Actions
Push the complete project to a GitHub repository with `main` as the branch. The included workflow builds an installable debug APK and exposes it under the workflow run's **Artifacts** section as:

`Apex-DMS-Native-Debug-APK`

## Local build
With Android SDK + JDK 17 + Gradle 8.9 installed:

```bash
gradle :app:testDebugUnitTest
gradle :app:lintDebug
gradle :app:assembleDebug
```

APK output:

`app/build/outputs/apk/debug/app-debug.apk`

## Demo data
The build is intentionally self-contained/offline and ships realistic demo business data. Integrations are represented as integration-ready/demo surfaces and do not claim live Tally/Razorpay/WhatsApp connectivity.
