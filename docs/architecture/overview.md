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

This is the target flow (not yet implemented — see `docs/adr/002-fhir-source-and-auth.md`):

1. User taps "Connect" in the app. App opens a Custom Tab to the sandbox's authorization endpoint, with a PKCE code challenge, requested scopes (`patient/*.read`, `launch/patient`, `openid`, `fhirUser`), and the app's redirect URI (`xyz.zyxwonderland.chart://oauth/callback`, reserved via `manifestPlaceholders["appAuthRedirectScheme"]`).
2. User picks a synthetic patient in the sandbox's picker UI and authorizes the app.
3. Sandbox redirects back to the app via the custom URI scheme with an authorization code.
4. App exchanges the code (plus the PKCE code verifier) for an access token and, per SMART's launch context, a `patient` parameter identifying which patient the token is scoped to.
5. App uses the access token as a Bearer token against the FHIR base URL returned by the sandbox's discovery document, fetching `Patient/{id}`, `Condition?patient={id}`, `MedicationRequest?patient={id}`, `Observation?patient={id}`.
6. Resources are held in ViewModel state only — never written to disk (ADR-004) — and rendered as Compose screens.
7. On logout or app close, in-memory state is cleared. If AppAuth issued a refresh token, it's kept only in Keystore-backed encrypted storage, separate from any clinical data.

## Module boundaries

- **UI (Compose screens):** presentation only, same discipline as MEND — no network or auth calls happen directly from a screen.
- **Auth layer:** wraps AppAuth-Android's `AuthorizationService`. Owns the PKCE flow, token exchange, and refresh-token lifecycle. This is the only place a secret-shaped value (the refresh token) is ever persisted, and only via Android's `EncryptedSharedPreferences`.
- **FHIR client:** a Ktor `HttpClient` (same client-setup pattern as MEND's `MendHttpClient`) that attaches the current access token as a Bearer header and deserializes FHIR R4 JSON resources. Read-only — CHART is a viewer, not an editor, so no `POST`/`PUT` FHIR interactions are in scope.

## Explicitly out of scope for this documentation phase

No Compose screens beyond the placeholder "coming soon" text, no AppAuth authorization-request wiring, no FHIR resource models or client calls, and no release pipeline exist yet in this repo. `app/build.gradle.kts` reserves the AppAuth redirect URI manifest placeholder and includes the AppAuth/Ktor dependencies so the next implementation pass has the scaffold already in place — mirroring how MEND's skeleton pre-added its dependencies before the update-checker feature used them.
