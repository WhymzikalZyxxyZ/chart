# CHART — Risk & Gap Register

Living document. Update as decisions are made or new risks surface.

## Synthetic-data-only boundary — CRITICAL

CHART is built and demoed exclusively against the SMART Health IT public sandbox, which contains only synthetic/test patient data by construction. This is a hard boundary, not a starting point to relax later.

**Mitigation stance:**
- The app must display a persistent, unmissable banner/label indicating all data shown is synthetic test data from a public sandbox, never real patient information.
- **This app must never be pointed at a real organization's FHIR endpoint or real patient data without a full security review** — encryption-at-rest analysis, access control review, audit logging, incident response plan, and almost certainly a formal HIPAA risk assessment and Business Associate Agreement posture, none of which this project has or is scoped to have. This isn't a "someday" caveat; it's the line between "portfolio demo" and "unauthorized handling of protected health information," which is a serious legal and ethical problem, not just a technical one.
- README and in-app UI must both state this plainly, not bury it in docs only a developer would read.

## OAuth2/PKCE implementation correctness — HIGH (once built)

An incorrectly implemented OAuth2 flow (weak PKCE verifier generation, missing state-parameter validation, loose redirect URI matching) is a real vulnerability class, not a theoretical one.

**Mitigation stance:** using AppAuth-Android (ADR-002) specifically to avoid hand-rolling this — it's a maintained, widely-used, security-focused library rather than custom crypto/state-handling code. Even so, the integration itself (how the app calls AppAuth, stores results, and validates the redirect) must be reviewed against AppAuth's own security guidance before this is considered done, not just "library used, therefore safe."

## No persistent storage — encryption/scope discipline — MEDIUM (once built)

ADR-004 commits to no local persistence of clinical data and Keystore-backed storage for the refresh token only. This is easy to violate accidentally (a debug log statement that prints a fetched resource, a crash reporter that captures in-memory state, an accidental `Room` entity added later for "just caching").

**Mitigation stance:** any future PR touching the auth or FHIR-client layers should be checked against ADR-004 explicitly. **Gap:** no automated check (lint rule, code review checklist item) enforces this yet — currently relies on manual discipline only.

## SMART sandbox availability — MEDIUM

Same caveat pattern as MEND's Overpass API dependency: SMART Health IT's public sandbox is shared community infrastructure with no SLA. It can be slow, down, or change its client-registration requirements without notice.

**Mitigation stance:** none needed beyond graceful error handling in the UI (not yet built) — this is accepted as an inherent property of using a free public sandbox, same tradeoff MEND already accepted for Overpass.

## Public client registration lifecycle — MEDIUM (open item)

SMART sandboxes typically require registering the app (client ID, redirect URI) either via a self-registration flow or a fixed demo client ID the sandbox provides. This hasn't been done yet.

**Mitigation stance:** **Gap, not yet resolved.** Registering CHART as a public client against the SMART Health IT sandbox (and documenting the resulting client ID/redirect URI in a non-secret config, since public clients have no secret to protect) is a prerequisite for building the actual auth flow — tracked as the next concrete implementation step after this documentation phase.

## FHIR resource display accuracy — LOW

Misrepresenting a FHIR resource's clinical meaning (e.g. mislabeling a `Condition`'s clinical status, or displaying a `MedicationRequest` as if it were an active prescription when its status is `stopped`) would be a real accuracy problem in a production clinical app.

**Mitigation stance:** lower stakes here than MEND's dietary-safety claims, since this is a read-only viewer of synthetic sandbox data and not advising any real-world action — but still worth getting right as a matter of demonstrating FHIR competence. No specific mitigation built yet since no display layer exists.

## No crash reporting / analytics pipeline — LOW

Same as MEND: no Play Store means no built-in crash reporting.

**Mitigation stance:** accepted for now, same reasoning as MEND's equivalent risk entry.
