# Brewkery Android

A native coffee and bakery ordering app built with Kotlin, Jetpack Compose, MVI and clean architecture.

Reference: https://vivekshah138.github.io/Brewkery/ (the repository's spelling is **Brewkery**, not `BrewKery`).

## Assignment submission

- Author: [Thilak Kumar](https://github.com/THILAK0520)
- Source: [Public repository](https://github.com/THILAK0520/BrewKery)
- [Download debug APK](https://github.com/THILAK0520/BrewKery/raw/refs/heads/main/deliverables/Brewkery-debug.apk) — Android 8.0 / API 26 or newer.
- 20 unit tests passed; lint has zero errors; application and instrumentation APKs build. Device UI tests remain unverified. See [VALIDATION.md](VALIDATION.md).
- Submit the APK as an attachment or Google Drive link with view access, alongside the repository URL, as requested in the assignment email. A recording is optional and is not included.

## Run and build

- Install JDK 17 and Android SDK platform 36. Minimum supported Android version: 8.0 / API 26.
- Open this directory in Android Studio and let Gradle sync, or set `JAVA_HOME` to JDK 17 and configure your SDK in the untracked `local.properties` file: `sdk.dir=/path/to/Android/Sdk`.
- Windows: `gradlew.bat testDebugUnitTest lintDebug assembleDebug`
- macOS/Linux: `sh gradlew testDebugUnitTest lintDebug assembleDebug`
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.
- Install on an emulator/device with `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
- Compose device tests: `gradlew.bat connectedDebugAndroidTest` (requires a connected Android device or emulator).
- Managed emulator tests: `gradlew.bat pixel2Api30DebugAndroidTest` (downloads a stable AOSP x86_64 image if needed; requires accepted SDK licenses and hardware virtualization). One integration test uses the live GitHub API and exports four screenshots through Gradle's additional test output directory.
- For a cloud-synced checkout that locks generated files, pass `-PbrewkeryBuildRoot=/path/to/local/build-cache`; app output then resides under that directory's `app` folder. Source files stay in this project.
- Alternatively add `brewkeryBuildRoot=C\:/path/outside/OneDrive` to ignored `local.properties` so IDE builds use it automatically. This machine is configured that way to avoid the reported `mergeDebugResources` access denial. Re-sync Gradle after changing the path; APKs then appear under `<brewkeryBuildRoot>/app/outputs/apk/debug/`.

## Product source of truth

The catalog comes exclusively from:

`https://raw.githubusercontent.com/VivekShah138/Brewkery/main/data.json`

Details come from:

`https://raw.githubusercontent.com/VivekShah138/Brewkery/main/api/items/{id}.json`

Categories, product names, descriptions, ingredients, prices, customizations, currency, delivery estimates and fees all come from these endpoints. The app does not package a hardcoded catalog, seed sample cart items, or invent fallback products. Product count and IDs are dynamic. API failure shows an error with a retry action. Synthetic fixtures exist only under test source sets.

Product photos use the API's `image_url`. The reference was measured in the browser: catalog thumbnails are 56 × 56 pixels and the hero is 347 × 160 pixels. The app uses 56 × 56 dp thumbnails and the same 347:160 hero aspect ratio at the available width. Coil resolves request dimensions from those layout constraints. `ContentScale.Crop` preserves proportions without stretching. Loading and failure placeholders keep the same dimensions. A failed image does not remove its product.

## Architecture

One Gradle app module keeps the assessment straightforward while separating responsibilities:

```text
com.brewkery.app
├── data          # Retrofit DTOs, validation/mapping, remote and session implementations
├── domain        # Immutable models, repository contracts, pricing/ordering use cases
├── di            # Hilt bindings and network configuration
└── presentation
    ├── common       # One-time effects
    ├── components   # Reusable stateless Compose controls
    ├── theme        # Shared colors and typography
    ├── navigation   # Menu → Detail → Cart → Order Status
    ├── menu
    ├── detail
    ├── cart
    └── order
```

Each interactive screen exposes immutable state through `StateFlow` and receives sealed intents through `accept`. ViewModels process intents; composables render state and dispatch actions. Navigation effects use buffered channels collected only while the destination lifecycle is started. Read-only order state comes from the session repository. Hilt injects dependencies by constructor; presentation depends on domain interfaces rather than Retrofit or mutable cart internals.

The process-scoped session repository owns cart, favorite IDs and the latest order. Cart operations publish new immutable snapshots. Checkout atomically creates an order snapshot and clears the cart; duplicate checkout/add taps are disabled. State survives rotation and ordinary background/foreground transitions while the process remains alive. It intentionally resets when the process is terminated. No Room or SQLite is used.

Shared components include headers, primary actions, product images/badges, option selectors, quantity steppers, loading and error states. All screens use the same theme, resource strings, spacing conventions, native back navigation and system insets. Content is scrollable and capped at 600 dp on larger displays. Controls preserve minimum 48 dp touch targets.

## Ordering rules

- Begin with an empty cart. Fetch every product from the catalog, not the prototype's four hardcoded examples.
- Search product name/description and apply category selection together.
- Default to the first available customization in each group. Keep subsequent choices until leaving the detail screen.
- Unit price = base price + selected size surcharge + selected milk/spread surcharge. Multiply by quantity for line total.
- Sugar/serving options are strings in the API. Even `Light Wildflower Honey (+0.40)` has no structured price field; like the reference calculation, labels do not add a surcharge. No parsing money from display text.
- Every add creates a separate configured cart line; decrementing a cart line to zero removes it. Quantity ranges from 1 to 99.
- Calculate currency amounts in integer cents. Round subtotal tax once with decimal `HALF_UP`: subtotal + configured delivery + configured tax. Empty cart has no delivery or tax.
- Place Order is a local simulation, matching the assessment. It generates a five-digit `#BK-` ticket, preserves the order snapshot, clears the cart, and shows `PREPARING`. There is no order/payment backend, payment processing or automatic delivery progression.
- Active order tracking on the menu opens the latest order. No history screen is added.
- The Android implementation fixes the reference JavaScript bug that resets selectors after a tap; it does not recreate the website's assignment navigation, fake phone bezel/status bar or submission controls.

## Security

- HTTPS only; Android network security configuration prohibits cleartext traffic and uses system certificate authorities. No trust-all TLS code or certificate bypasses.
- Product image URLs must use `https://images.unsplash.com` without embedded credentials or custom ports. Unsupported URLs show a placeholder.
- The app requests `INTERNET`; AndroidX also declares a signature-only permission for its private receivers. Only the launcher activity is exported. Debug preview/test activities are explicitly non-exported; the framework profile installer receiver requires the system `DUMP` permission. Backup is disabled; release builds are non-debuggable and minified.
- No credentials, authentication, personal/payment data, analytics, email sending, or secrets in source. API paths are fixed and item IDs are integers.
- Validate API-required fields, nonnegative prices, tax bounds, unique IDs and category references. Reject invalid payloads rather than fabricating values.
- Preserve coroutine cancellation, bound network timeouts, display user-facing errors, and omit request/response logging.
- Debug APKs are for assessment/testing. Security controls do not turn simulated checkout into a production commerce service.

## Tests and verification

Unit tests cover decimal pricing, tax rounding, API field mapping, dynamic product IDs/counts, invalid data and unsafe image URLs, cart changes, configuration variants, order snapshots, favorites, MVI loading/error/retry and duplicate actions. Retrofit contract tests use MockWebServer without requiring the live API. Compose instrumentation tests cover empty-cart checkout and server-provided detail options.

See [VALIDATION.md](VALIDATION.md) for the actual build/test results and device-testing limitations. Tests and APK generation do not by themselves certify visual parity; screen comparison requires running the app on an Android device/emulator.

## AI workflow

Tool used: OpenAI Codex, to inspect the public assessment, implement Kotlin/Compose code, generate tests, and investigate build results. No other AI tool is claimed.

Actual instructions used in this conversation included:

1. “follow the MVI pattern.” and “maintain the coding structure as clean architecture.”
2. “make sure that all products are coming from the JSON mention in the Git Hub.” and “make sure that all the images you're creating or taking in the correct size.”
3. “from the pervious plan and response start implement.”

### Bug caught and fixed

An IDE build failed at `mergeDebugResources` with `AccessDeniedException` on generated files inside OneDrive. A command-line workaround did not apply automatically to IDE builds. The Gradle script now reads an external build path from ignored `local.properties`, while permitting a command-line override. An explicit `Properties` import corrected a Gradle script namespace collision caught during verification. The previously failing task and subsequent full build/test checks passed.

What went well: inspection distinguished the live six-product API from the four-product JavaScript demo, so the app loads products dynamically rather than copying the demo's embedded catalog.

What needed correction: the first implementation included the entire extended Material icon library for just a few symbols. A stalled build's worker stacks showed D8 spending time transforming dependencies. The unnecessary library was replaced with core icons and small vector drawables for the bag and cup, reducing build and APK work. The initial site lookup also needed correction from `BrewKery` to the README's `Brewkery` URL.

Source repository: https://github.com/THILAK0520/BrewKery. The verified debug APK is included in `deliverables/` and linked above. Build instructions also generate it from source. No submission email is sent. A screen recording is not included.
