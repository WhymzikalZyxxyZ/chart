# CHART

**C**linical **H**istory & **A**ccess **R**ecords **T**ool

CHART is an Android app that demonstrates SMART on FHIR integration — the OAuth2/PKCE "SMART App Launch" pattern that real EHR vendors (Epic, Cerner/Oracle Health) require of third-party clinical apps — against a free public sandbox. It's a portfolio piece built specifically to show healthcare-integration competence, not a product.

> ⚠️ **All data shown by this app is synthetic test data from a public sandbox — never real patient information.** This app is not built, reviewed, or authorized to handle real protected health information (PHI), and must never be pointed at a real organization's FHIR endpoint without a full security review. See [`docs/RISKS.md`](docs/RISKS.md).

> **This repo is currently in its design/documentation phase.** No app functionality is implemented yet — see [Status](#status) below.

## Status

This repository currently contains:
- A minimal, buildable Android/Compose skeleton (`app/`) with no feature logic
- Reserved manifest surface and dependencies (AppAuth-Android, Ktor) for the SMART App Launch flow, not yet wired up
- Full design documentation (this README, ADRs, architecture doc, risk register)

Not yet built: the actual SMART App Launch OAuth2/PKCE flow, FHIR resource models and fetching, any Compose screens beyond a placeholder, and public client registration against the SMART sandbox.

## Why these choices

Full reasoning lives in `docs/adr/`, but in short:

| Decision | Choice | Why |
|---|---|---|
| Platform | Android-only, Kotlin + Jetpack Compose | Consistent with [MEND](https://github.com/WhymzikalZyxxyZ/mend)'s Kotlin portfolio pattern, zero-cost sideload distribution ([ADR-001](docs/adr/001-platform-and-distribution.md)) |
| FHIR source & auth | SMART on FHIR sandbox, SMART App Launch (OAuth2 + PKCE via AppAuth-Android) | This is the actual integration pattern real EHR vendors require — the single highest-signal choice in this project ([ADR-002](docs/adr/002-fhir-source-and-auth.md)) |
| Backend | None — public client + PKCE talks directly to the sandbox | PKCE exists specifically so mobile apps don't need a secret-holding backend; adding one would misrepresent the architecture ([ADR-003](docs/adr/003-no-backend-required.md)) |
| Local storage | None — clinical data is in-memory only, cleared on logout | Demonstrates PHI-handling discipline even though the sandbox data is synthetic — the app behaves as if it were real ([ADR-004](docs/adr/004-no-persistent-storage.md)) |

## Architecture

See [`docs/architecture/overview.md`](docs/architecture/overview.md) for the SMART App Launch sequence and module boundaries.

## Risks & known gaps

See [`docs/RISKS.md`](docs/RISKS.md). The synthetic-data-only boundary is the most important thing in this document — read it before touching the auth or FHIR-client layers.

## Building

Requires Android Studio (bundled Gradle) or a local Gradle install.

```
git clone https://github.com/WhymzikalZyxxyZ/chart.git
cd chart
# Open in Android Studio, let it sync, then Run.
# Or, with Gradle installed locally:
gradle assembleDebug
```

Min SDK 26, target/compile SDK 35, Kotlin 2.0.21, Jetpack Compose.

## Downloading

Once a release is cut, the APK will be attached to this repo's [Releases](https://github.com/WhymzikalZyxxyZ/chart/releases) page, and linked from the Technologist section of [zyxwonderland.xyz](https://zyxwonderland.xyz). Installing requires enabling "install unknown apps," since this isn't distributed through the Play Store.

## Updating

Same as MEND: no Play Store, so no automatic-update channel. This is tracked as a follow-up — CHART doesn't yet have its own in-app update checker.

## Cutting a release

The release workflow (`.github/workflows/release.yml`) builds and signs the release APK and attaches it to a GitHub Release whenever a tag matching `v*.*.*` is pushed:

```
git tag v0.1.0
git push origin v0.1.0
```

This requires a signing keystore, set up once — **CHART has its own keystore, separate from MEND's.** Each app gets its own signing identity rather than sharing one, so compromising or losing one app's key never affects another's.

1. Generate a keystore (requires a JDK): `keytool -genkeypair -v -keystore chart-release.keystore -alias chart -keyalg RSA -keysize 2048 -validity 10000`
2. **Back this file up somewhere safe outside git.** It's the app's permanent signing identity — losing it means any future release can no longer update an already-installed copy of the app.
3. Base64-encode it and add these as repo secrets (Settings → Secrets and variables → Actions):
   - `RELEASE_KEYSTORE_BASE64` — `base64 -w0 chart-release.keystore`
   - `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` — matching what you set in step 1

Without these secrets, the workflow fails fast at the "Decode release keystore" step rather than silently producing an unsigned APK.

## License

MIT — see [LICENSE](LICENSE).
