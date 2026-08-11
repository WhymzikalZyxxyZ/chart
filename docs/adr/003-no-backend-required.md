# ADR-003: No Backend Required

**Date:** 2026-08-11
**Status:** Accepted
**Deciders:** Zyxxyz

---

## Context

[MEND](https://github.com/WhymzikalZyxxyZ/mend) needed a Cloudflare Worker backend specifically to hold third-party API secrets server-side and apply caching/rate-limiting the client couldn't safely do itself (see MEND's ADR-004). CHART's architecture is different enough from MEND's that this needs its own decision rather than defaulting to "copy MEND's backend."

## Decision Drivers

- Don't introduce infrastructure that isn't actually needed — a backend is a real cost in complexity and (potentially) hosting, even at $0 monetary cost
- SMART App Launch has a specific provision for exactly this situation: public clients

## Options Considered

### Option A — No backend; app talks directly to the SMART authorization server and FHIR API
SMART App Launch supports registering as a "public client" — an app that, by design, cannot keep a secret (true of any mobile app, since anything shipped in an APK can be extracted). Public clients use OAuth2 Authorization Code + PKCE specifically because PKCE removes the need for a client secret: the proof-of-possession is a dynamically generated code verifier/challenge pair, not a static secret. The SMART Health IT sandbox supports registering CHART as a public client.

**Pros:** No backend to build, host, or secure. No secrets to manage at all — nothing in this architecture needs a place to hide a secret, because PKCE was designed to eliminate that need for exactly this kind of client. Simpler, more reviewable architecture; a healthcare-org reviewer can trace the entire auth flow without also having to trust a middle-tier server they can't see.
**Cons:** No server-side layer for caching FHIR responses, rate-limiting, or centralizing logic if the app ever needs to support multiple FHIR sources with different auth requirements.

### Option B — Cloudflare Worker as a token-broker/proxy (MEND's pattern)
A Worker holds a confidential client secret (if the sandbox is later configured as a confidential client) and proxies FHIR calls, similar to MEND's Overpass/recipe-API proxy.

**Pros:** Centralizes any future secret management; enables server-side caching.
**Cons:** Unnecessary for the actual chosen flow (public client + PKCE) — would be solving a problem this architecture doesn't have. Also weakens the specific signal Option A sends: that the app correctly understands why mobile/public clients use PKCE instead of confidential-client patterns.

## Decision

**Chosen option: Option A — no backend.** CHART talks directly to the SMART authorization server and FHIR REST API as a public client using PKCE. This is architecturally simpler than MEND, deliberately — the two apps have genuinely different needs, not the same need solved two different ways.

## Consequences

**Positive:**
- Zero hosting to build/maintain for this app, an even stronger cost story than MEND
- Simpler, more auditable architecture — no server-side component a reviewer has to trust without seeing
- Correctly demonstrates understanding of why public clients use PKCE rather than reaching for a backend out of habit

**Negative / accepted tradeoffs:**
- No server-side caching layer; every resource fetch hits the FHIR sandbox directly (acceptable at demo scale)
- If CHART ever needs to integrate a FHIR source that only supports confidential clients, this decision would need revisiting

**Risks:**
- None beyond what ADR-002 already covers — this ADR is a scope-reduction decision, not a new risk surface

## Notes

- Revisit if a future FHIR source requires confidential-client registration (no PKCE support) — would reopen Option B.
