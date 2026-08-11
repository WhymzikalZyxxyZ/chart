# ADR-001: Platform & Distribution Strategy

**Date:** 2026-08-11
**Status:** Accepted
**Deciders:** Zyxxyz

---

## Context

CHART is a portfolio piece aimed specifically at demonstrating healthcare-integration competence — SMART on FHIR, the standard EHR app-launch pattern — to reviewers at healthcare organizations. It needs to run on a phone, be written in Kotlin, and be downloadable from zyxwonderland.xyz, same baseline requirements as [MEND](https://github.com/WhymzikalZyxxyZ/mend).

## Decision Drivers

- Zero ongoing cost, same hard requirement as every other app on this site
- Consistency with the site's existing Android-app distribution precedent (The Warden, MEND) — one pattern, not a new one per app
- This is explicitly a demonstration piece for a specific audience (healthcare-org technical reviewers), which affects how much install-friction is acceptable

## Options Considered

### Option A — Android-only, Jetpack Compose, sideloaded via GitHub Releases
Same shape as MEND: native Android/Compose app, self-signed APK attached to GitHub Releases, linked from the site.

**Pros:** Zero cost, reuses the exact toolchain, signing pipeline, and site-linking pattern already proven twice (The Warden, MEND) — no new infrastructure to build or explain. Keeps a consistent "Kotlin portfolio" narrative across pieces.
**Cons:** A reviewer has to sideload an APK just to look at a demo app, which is real friction for something whose whole purpose is being looked at quickly.

### Option B — Web app hosted on zyxwonderland.xyz
A browser-based FHIR viewer, deployed the way `the-locator` and `elinal` are (Cloudflare Workers/Pages).

**Pros:** Zero install friction — a reviewer just opens a URL. Also sidesteps Android-specific OAuth redirect complexity (a web app uses a normal HTTPS redirect, no custom URI scheme).
**Cons:** Doesn't fit the Kotlin-portfolio narrative this app was specifically framed around; would mean building a second, unrelated stack just for this one piece.

## Decision

**Chosen option: Option A — Android-only Kotlin/Compose, sideloaded via GitHub Releases**, explicitly chosen over the lower-friction web option to keep CHART consistent with MEND's Kotlin/Android portfolio pattern rather than fragmenting the site's app pieces across stacks. The install-friction tradeoff from Option A's "Cons" is accepted as-is here, not mitigated — see Notes.

## Consequences

**Positive:**
- Zero incremental cost or new infrastructure — reuses MEND's proven Gradle setup, signing pipeline, and site-integration pattern verbatim
- Consistent Kotlin narrative across the site's app portfolio pieces

**Negative / accepted tradeoffs:**
- Install friction for reviewers, same as MEND and The Warden — not mitigated by this ADR
- Android's OAuth2/PKCE redirect handling (Custom Tabs + a reserved URI scheme) is inherently more moving parts than a web app's plain HTTPS redirect — see ADR-002

**Risks:**
- If reviewer friction turns out to matter more in practice than expected, this decision may need revisiting toward a web-based demo mirror later — not blocking for now

## Notes

- See [ADR-002](002-fhir-source-and-auth.md) for the FHIR data source and SMART App Launch auth strategy this platform choice has to support.
- Signing/release pipeline mirrors MEND's `.github/workflows/release.yml` and `app/build.gradle.kts` signing config exactly — see that repo for the release-cutting process once this repo needs one.
