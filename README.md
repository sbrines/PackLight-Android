# PackLight — Android

**Ultralight backpacking trip planner for Android 8.0+ (API 26+)**

> The modern alternative to Lighterpack. Native Kotlin + Jetpack Compose, offline-first.

---

## Features

| Feature | Details |
|---|---|
| **Gear Inventory** | Full gear library with category, brand, weight, quantity, and notes |
| **URL Auto-Import** | Paste a product URL — weight fetched from Shopify stores, REI, Backcountry via Jsoup |
| **Pack Builder** | Build trip-specific pack lists; mark items worn or consumable, sticky category headers |
| **Weight Calculator** | Base / worn / consumable / total weight with SUL/UL/Lightweight/Traditional classification |
| **Resupply Logistics** | Plan resupply boxes with mile markers and status tracking |
| **Gear Recommendations** | Route-aware recommendations based on elevation, season, terrain, trip duration |
| **Lighterpack Import/Export** | Import CSV from lighterpack.com; export gear list via share sheet |
| **Material 3 UI** | Dynamic color, NavigationBar tabs, bottom sheets throughout |

---

## Tech Stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Room** for local persistence
- **Hilt** for dependency injection
- **Kotlin Coroutines** + **StateFlow**
- **Jsoup** for HTML parsing (URL weight extraction)
- **OkHttp** for networking
- **Vico** for charts
- **GitHub Actions** for CI (unit tests + APK build + lint)

---

## Architecture

```
app/src/main/java/com/stephenbrines/packlight/
├── data/
│   ├── db/            # Room database, DAOs, type converters
│   ├── model/         # GearItem, GearCategory, Trip, PackList, PackListItem,
│   │                  #   ResupplyPoint, ResupplyItem
│   └── repository/    # GearRepository, TripRepository
├── service/           # WeightCalculator, WeightParser, UrlMetadataFetcher,
│                      #   GearRecommendationEngine, LighterpackService
├── ui/
│   ├── gear/          # GearListScreen, AddGearSheet, GearViewModel, ImportExportActions
│   ├── trips/         # TripListScreen, TripDetailScreen, PackListScreen, TripViewModel
│   ├── weight/        # WeightDashboardScreen, WeightViewModel
│   └── theme/         # Material 3 theme with dynamic color
└── di/                # Hilt AppModule
```

**Pattern:** MVVM — `ViewModel` holds `StateFlow<UiState>`, UI collects with `collectAsStateWithLifecycle`.

---

## URL Weight Fetching

| Site | Method |
|---|---|
| Shopify stores (Zpacks, GG, ULA, MLD, Six Moon, Tarptent, HMG) | Public `/products/{slug}.json` API |
| REI | SSR HTML — JSON-LD + specs table via Jsoup |
| Backcountry | `window.__INITIAL_STATE__` JSON blob |
| Other sites | Open Graph + JSON-LD schema.org |

---

## Requirements

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Gradle 8.10.2 (wrapper included)
- Android SDK 35 (target), 26 (min)

## Building

```bash
git clone https://github.com/sbrines/PackLight-Android
cd PackLight-Android
./gradlew assembleDebug
```

Install on device/emulator:
```bash
./gradlew installDebug
```

---

## Tests

```bash
./gradlew test
```

**Test coverage:**
- `WeightParserTest` — all weight format strings
- `WeightCalculatorTest` — classification, categories, worn/consumable split
- `GearRecommendationTest` — 8 scenarios: required gear, alpine/desert/winter, extended/long-distance
- `LighterpackServiceTest` — import/export round-trip, quoted fields, oz conversion

---

## CI (GitHub Actions)

Three jobs on push to `main` / PR:
1. **Unit Tests** — `./gradlew test`
2. **Build** — `./gradlew assembleDebug` → uploads APK artifact
3. **Lint** — `./gradlew lintDebug` → uploads HTML report

> **Note:** GitHub Actions workflow files require `workflow` OAuth scope. Run `gh auth refresh -h github.com -s workflow` then `git push` if the `.github/` directory isn't tracked.

---

## FileProvider Setup

For the Lighterpack CSV export share sheet, add to `AndroidManifest.xml`:

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

And create `res/xml/file_paths.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="cache" path="." />
</paths>
```

---

## Related Repos

- **iOS + macOS**: [PackLight-iOS](https://github.com/sbrines/PackLight-iOS)
- **Landing page**: [PackLight-Web](https://github.com/sbrines/PackLight-Web)
