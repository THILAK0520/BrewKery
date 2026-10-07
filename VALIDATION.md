# Validation — 7 October 2026

Repeated on 7 October from the relocated folder: all 90 Gradle tasks succeeded. IntelliJ inspection timed out; these are Gradle results, not device runtime results.

## Verified

- All 20 JVM unit tests passed with zero failures. Tests cover pricing and tax rounding, session/cart/order behavior, API validation, Retrofit contracts and MVI loading/retry/actions.
- Android lint passed with zero errors. The remaining 18 warnings recommend dependency upgrades; no application correctness warnings remain.
- The debug application and instrumentation APK compile and package successfully using JDK 17, Gradle 8.13, Android Gradle Plugin 8.12.0 and SDK 36.
- The application APK signature verifies successfully. Minimum API 26; target API 36; application ID `com.brewkery.app`.
- Source network configuration requires HTTPS, disables backup and uses system certificate authorities. Debug preview/test activities are explicitly non-exported. The framework profile installation receiver is protected by the system `DUMP` permission.
- Product images retain the JSON URLs and use constrained 56 dp square thumbnails and 347:160 detail images with proportional cropping.

## Device verification limitation

The instrumentation suite is included and its APK builds, but it has **not run on a device**. No connected Android device or existing emulator was available. The optimized test image required an unaccepted preview license, which was not accepted. The stable API 30 AOSP image recognized the existing SDK license, but its download stalled with a zero-byte archive and was cancelled.

Consequently, runtime UI behavior, screenshots, actual device rotation and exact visual parity remain unverified. No runtime screenshots are represented as evidence. Run `gradlew.bat connectedDebugAndroidTest` with a connected device, or `gradlew.bat pixel2Api30DebugAndroidTest` once the stable image can download.

The included live API integration test covers Menu → configured Item Detail → Cart → Order Status → Menu, activity recreation, and the cleared cart. It also captures all four screens. Two isolated Compose tests cover empty-cart checkout and API-defined option dispatch.

## Delivery

The source project is in this directory and is published at https://github.com/THILAK0520/BrewKery. The installable assessment APK is included at `deliverables/Brewkery-debug.apk`; a fresh clone can generate it with the documented build command. Checkout is a local simulation; no payment/order backend is claimed. No email was sent.

APK SHA-256: `922CA9531E25697432DB48D8060BAE30FFF9F1D4D68A43179A28AFDB10B1D116`.
