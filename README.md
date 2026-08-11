# CHART

**C**linical **H**istory & **A**ccess **R**ecords **T**ool

CHART is an Android app that demonstrates SMART on FHIR integration — the OAuth2/PKCE "SMART App Launch" pattern that real EHR vendors (Epic, Cerner/Oracle Health) require of third-party clinical apps — against a free public sandbox. It's a portfolio piece built specifically to show healthcare-integration competence, not a product.

> ⚠️ **All data shown by this app is synthetic test data from a public sandbox — never real patient information.** This app is not built, reviewed, or authorized to handle real protected health information (PHI), and must never be pointed at a real organization's FHIR endpoint without a full security review. See [`docs/RISKS.md`](docs/RISKS.md).

## Status

The SMART App Launch flow is implemented and working end-to-end against the SMART Health IT public sandbox:
- Connect → Custom Tab login/patient-picker → OAuth2 Authorization Code + PKCE exchange (AppAuth-Android) → fetch and display the authenticated `Patient` resource
- Refresh token persisted in Keystore-backed `EncryptedSharedPreferences` (`auth/TokenStore.kt`); silently resumed on next app launch, no re-login needed
- No client registration against the sandbox required or done — see [ADR-002](docs/adr/002-fhir-source-and-auth.md)

Not yet built: `Condition`/`MedicationRequest`/`Observation` fetching and screens (the FHIR client is written generically enough to extend, not redesign), and automatic mid-session access-token refresh on a 401 (only refresh-on-app-launch exists today).

This repository also has an in-app update checker (`xyz.zyxwonderland.chart.update`) and a signed release pipeline — see [Updating](#updating) and [Cutting a release](#cutting-a-release) below.

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

Since CHART isn't on the Play Store, there's no automatic-update channel. On launch, the app pings this repo's [GitHub Releases API](https://api.github.com/repos/WhymzikalZyxxyZ/chart/releases/latest) (at most once every 24 hours, silently ignoring failures) and shows a dismissible in-app banner if a newer version is available — same mechanism as [MEND](https://github.com/WhymzikalZyxxyZ/mend). "View" just opens the Release page in a browser; nothing auto-downloads or auto-installs.

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
