# CHART — Architecture Overview

See `docs/adr/` for the reasoning behind each decision referenced here.

## System shape

```
┌──────────────────────────────────────────┐
│  Android app (app/)                        │
│  Kotlin + Jetpack Compose                  │
│                                             │
│  ┌───────────────────────────────────────┐│
│  │ UI (Compose screens)                    ││
│  │  Login/launch · Patient chart list      ││
│  │  · Resource detail views                ││
│  └────────────────┬──────────────────────┘│
│                    │                        │
│  ┌────────────────▼──────────────────────┐│
│  │ Auth layer (AppAuth-Android)            ││
│  │  SMART App Launch: Standalone Launch,   ││
│  │  OAuth2 Authorization Code + PKCE       ││
│  │  via Custom Tabs                        ││
│  └────────────────┬──────────────────────┘│
│                    │ Bearer token           │
│  ┌────────────────▼──────────────────────┐│
│  │ FHIR client (Ktor)                      ││
│  │  In-memory only — no Room, no local     ││
│  │  persistence of clinical data           ││
│  │  (ADR-004)                              ││
│  └────────────────┬──────────────────────┘│
└───────────────────┼───────────────────────┘
                     │ HTTPS
                     ▼
┌──────────────────────────────────────────┐
│  SMART Health IT public sandbox            │
│  Authorization server + FHIR R4 API        │
│  (no app-owned backend in between —        │
│   see ADR-003)                             │
└──────────────────────────────────────────┘
```

## SMART App Launch: Standalone Launch sequence

**Implemented** — see `docs/adr/002-fhir-source-and-auth.md`:

1. User taps "Connect" in the app (`ui/launch/LaunchScreen.kt`). The app fetches `{FHIR_BASE_URL}/.well-known/smart-configuration` (`auth/SmartConfigurationClient.kt`) to discover the sandbox's authorize/token endpoints, then opens a Custom Tab to the authorization endpoint with a PKCE code challenge (generated automatically by AppAuth-Android), requested scopes (`launch/patient`, `openid`, `fhirUser`, `offline_access`, `patient/*.read`), and the app's redirect URI (`xyz.zyxwonderland.chart:/oauth2redirect`, reserved via `manifestPlaceholders["appAuthRedirectScheme"]`).
2. User picks a synthetic patient in the sandbox's picker UI and authorizes the app. No client registration exists or is needed — SMART Health IT's sandbox explicitly supports unregistered public clients (`client_id = "chart-android"` is just a self-chosen string).
3. Sandbox redirects back to the app via the custom URI scheme with an authorization code; AppAuth's manifest-merged `RedirectUriReceiverActivity` routes it back to `MainActivity`'s `ActivityResultLauncher`.
4. App exchanges the code (plus the PKCE code verifier) for an access token and, per SMART's launch context, a `patient` parameter identifying which patient the token is scoped to (`auth/SmartAuthManager.kt`).
5. App uses the access token as a Bearer token against the FHIR base URL, fetching `Patient/{id}` (`fhir/FhirClient.kt`). `Condition`/`MedicationRequest`/`Observation` fetches are not built yet — see ADR-002 Notes.
6. The fetched `Patient` is held in `SmartLaunchViewModel` state only — never written to disk (ADR-004) — and rendered by `LaunchScreen`.
7. If AppAuth issued a refresh token, it's persisted via `auth/TokenStore.kt` (`EncryptedSharedPreferences`, Keystore-backed) — refresh token, patient id, and token endpoint only, never clinical data. On next app launch, `SmartLaunchViewModel.init` silently exchanges it for a fresh access token and re-fetches the patient, skipping the Connect screen. "Disconnect" clears the store and returns to the Idle state.

## Module boundaries

- **UI (Compose screens):** presentation only, same discipline as MEND — no network or auth calls happen directly from a screen. `LaunchScreen` is driven entirely by `SmartLaunchViewModel`'s `LaunchUiState` (`Idle` / `Connecting` / `Connected` / `Error`).
- **Auth layer** (`auth/`): `SmartAuthManager` wraps AppAuth-Android's `AuthorizationService`. Owns the PKCE flow, token exchange, and refresh-token lifecycle. `TokenStore` is the only place a secret-shaped value is ever persisted, and only via `EncryptedSharedPreferences`.
- **FHIR client** (`fhir/`): a Ktor `HttpClient` (`network/ChartHttpClient.kt`, same client-setup pattern as MEND's `MendHttpClient`) that attaches the current access token as a Bearer header and deserializes FHIR R4 JSON resources. Read-only — CHART is a viewer, not an editor, so no `POST`/`PUT` FHIR interactions are in scope.

## Explicitly out of scope (not yet built)

`Condition`, `MedicationRequest`, and `Observation` fetching/screens (the FHIR client function is generic enough that adding these is additive); automatic mid-session access-token refresh on a 401 (only refresh-on-app-launch exists); any UI beyond the single Connect/Connected screen.
