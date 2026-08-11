# ADR-002: FHIR Data Source & Auth Strategy

**Date:** 2026-08-11
**Status:** Accepted
**Deciders:** Zyxxyz

---

## Context

CHART's entire purpose is demonstrating a real EHR-integration pattern, not just displaying clinical-looking data. The data source and auth mechanism chosen are the single most important decisions in this project — they determine whether this reads as "knows FHIR resource shapes" or "understands how apps actually get authorized to see patient data inside a real health system."

## Decision Drivers

- Must stay free — no billing account, no paid sandbox tier
- The auth pattern should be the one real EHR vendors (Epic App Orchard, Cerner/Oracle Health Code) actually require of third-party apps, not a simplified stand-in
- Data displayed must be unambiguously non-real (synthetic/test), given the sensitivity of anything shaped like patient data — see `docs/RISKS.md`

## Options Considered

### Option A — SMART on FHIR sandbox, SMART App Launch (OAuth2 + PKCE)
Implements the SMART App Launch "Standalone Launch" sequence against SMART Health IT's free public sandbox (`launch.smarthealthit.org` / a sandbox-issued FHIR base URL): app initiates an OAuth2 Authorization Code + PKCE flow via Custom Tabs, receives an access token scoped to a specific synthetic patient, then calls FHIR REST endpoints (`Patient`, `Condition`, `MedicationRequest`, `Observation`, etc.) with that token.

**Pros:** This is literally the integration pattern EHR vendors require — SMART App Launch is an HL7-published, industry-standard specification, not a CHART-specific design. Directly demonstrates OAuth2/PKCE competence plus FHIR resource modeling. The sandbox is free, requires no billing account, and is explicitly built for exactly this kind of demo/dev use.
**Cons:** Meaningfully more implementation work than a plain authenticated REST call — a full OAuth2 Authorization Code + PKCE flow, Custom Tabs integration, redirect URI handling, token refresh.

### Option B — HAPI FHIR public test server, unauthenticated REST
Direct REST calls against the community-run HAPI FHIR public test server, no auth flow at all.

**Pros:** Much simpler to build — no OAuth2, no redirect handling, no token management. Still demonstrates FHIR resource modeling and REST semantics.
**Cons:** Skips the actual distinctive signal: any developer can call an unauthenticated public FHIR server. It says nothing about whether the app understands how real clinical systems gate access to patient data, which is the whole point of aiming this piece at healthcare organizations specifically.

### Option C — Static synthetic data (Synthea-generated bundles), no live server
Pre-generated realistic synthetic patient records (Synthea is a widely-used open-source synthetic patient generator) bundled with the app, no network dependency at all.

**Pros:** Zero external dependency risk — works fully offline, no server ever goes down or changes. Full control over data quality and completeness.
**Cons:** No live-integration story whatsoever — this is a data-modeling exercise, not an integration demo. Weakest option against this project's actual goal.

## Decision

**Chosen option: Option A — SMART on FHIR sandbox with SMART App Launch (OAuth2 + PKCE)**, using [AppAuth-Android](https://github.com/openid/AppAuth-Android) (OpenID Foundation's official Android OAuth2/OIDC client library) rather than hand-rolling the PKCE flow — hand-rolling OAuth2 crypto/state-handling is exactly the kind of thing that should use a maintained, security-reviewed library instead of custom code.

## Consequences

**Positive:**
- Directly demonstrates the actual integration pattern healthcare orgs care about, not a simplified stand-in
- AppAuth-Android is free, open source (Apache 2.0), and handles the security-sensitive parts (PKCE code verifier/challenge generation, state parameter, redirect validation) rather than leaving that to custom code

**Negative / accepted tradeoffs:**
- Significantly more implementation surface than Options B or C — full OAuth2 flow, Custom Tabs, redirect URI intent-filter, token refresh handling
- The standalone launch flow, token exchange, silent refresh-token resume, and a minimal `Patient` fetch are now implemented (`app/src/main/kotlin/xyz/zyxwonderland/chart/auth/`, `fhir/`, `ui/launch/`) — see Notes for what's still deliberately out of scope

**Risks:**
- The SMART Health IT public sandbox is shared community infrastructure with no SLA — same caveat as MEND's Overpass API dependency, see `docs/RISKS.md`
- All data behind this flow is synthetic/test data by construction of the sandbox; this app must never be pointed at a real organization's FHIR endpoint without a full security review — see `docs/RISKS.md`, this is the single most important risk in this project

## Notes

- SMART App Launch spec: the "Standalone Launch" sequence (as opposed to "EHR Launch," which requires being invoked from inside an EHR's UI) is the correct one for a self-contained mobile app — see `docs/architecture/overview.md` for the sequence diagram.
- **Implemented**: discovery (`.well-known/smart-configuration`), the Custom Tabs authorization request with PKCE, authorization code → token exchange, silent refresh-token resume on app launch, and a `Patient` resource fetch/display as end-to-end proof of the round-trip.
- **Client registration**: none needed. SMART Health IT's public sandbox (`smart-on-fhir/smart-launcher-v2`) explicitly supports unregistered public clients for exactly this kind of testing — `client_id` is the self-chosen string `chart-android`, not a credential.
- **Deliberately not built yet**: `Condition`/`MedicationRequest`/`Observation` screens (the FHIR client function is generic enough that adding these is additive, not a redesign) and automatic mid-session access-token refresh on a 401 (only refresh-on-app-launch is implemented; a token expiring mid-session currently surfaces as an error rather than silently recovering).
