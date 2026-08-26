# Compile Fix — 2026-08-26

Fixes applied after GitHub Actions run 89243672303:

- Kotlin Gradle plugin upgraded from 1.9.25 to 2.1.20.
- JetBrains Compose compiler Gradle plugin added for Kotlin 2.x.
- Removed legacy `kotlinCompilerExtensionVersion = 1.5.15`.
- Truecaller OAuth imports corrected to `com.truecaller.android.sdk.oAuth.*`.
- `TcOAuthCallback.onVerificationRequired(...)` implemented.
- `MainActivity` changed to `FragmentActivity` and fragment dependency added for Truecaller ActivityResult integration.
- `AccessScreen` now obtains a `FragmentActivity`.
- Missing `androidx.compose.ui.unit.dp` import added in `ProductArtwork.kt`.
- Release CI fingerprint updated to the permanent locally-generated SHA1:
  `D4:1D:9F:BA:97:B0:74:C1:D4:74:B4:0B:4E:CD:EB:A0:4C:C2:76:1B`.

The Truecaller SDK remains 3.2.1.
