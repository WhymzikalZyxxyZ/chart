# ADR-004: No Persistent Local Storage of Clinical Data

**Date:** 2026-08-11
**Status:** Accepted
**Deciders:** Zyxxyz

---

## Context

CHART fetches and displays FHIR resources that are shaped exactly like real patient data (Patient demographics, Conditions, Medications, Observations) even though, per ADR-002, the sandbox only ever contains synthetic/test data. How the app handles that data locally is itself a signal to a healthcare-org reviewer, independent of whether the specific bytes are real.

## Decision Drivers

- The app should behave the way it would need to behave if it were ever pointed at a real FHIR source — demonstrating that discipline is part of the point of this portfolio piece, not an afterthought
- Simplicity: this is a viewer, not a records-management system

## Options Considered

### Option A — No persistent storage; in-memory only, cleared on logout/app close
Fetched FHIR resources live in memory (ViewModel state) for the duration of a session and are discarded on logout or process death. Nothing is written to Room, SharedPreferences, or any other persistent store. Only the OAuth refresh token (if AppAuth's flow issues one) is persisted, and only in Android's encrypted storage (`EncryptedSharedPreferences` / Keystore-backed), never the clinical data itself.

**Pros:** Directly demonstrates PHI-handling discipline: nothing sensitive survives past the session, so there's no local data-at-rest to secure, back up, or accidentally leak via a device backup. This is the same posture a real clinical app would need — treats the sandbox as if it were real, which is the entire credibility argument for this portfolio piece.
**Cons:** No offline viewing — closing the app means re-fetching everything, and re-authenticating if the refresh token has also expired or been cleared.

### Option B — Cache FHIR resources locally (Room), same pattern as MEND
Persist fetched resources for offline viewing and faster reloads, matching MEND's local-cache architecture.

**Pros:** Better UX (offline access, instant reload), consistent with MEND's data-layer pattern.
**Cons:** Wrong choice specifically because this is clinical data, not recipe data — persisting it locally without encryption-at-rest, remote-wipe capability, and a data-retention policy is exactly the kind of shortcut a real healthcare deployment could never accept. Copying MEND's caching pattern here would undercut the project's own thesis.

## Decision

**Chosen option: Option A — no persistent storage of clinical data**, in-memory only, cleared on logout or app close. Only the OAuth refresh token is persisted, and only via Android's Keystore-backed encrypted storage — never in plaintext, and never bundled with any clinical data.

## Consequences

**Positive:**
- No local data-at-rest to secure — the single largest source of mobile health-app data breaches (lost/stolen devices with unencrypted local caches) is architecturally not possible here
- Consistent, demonstrable discipline: this app behaves as if the data were real, which is the actual credibility signal for a healthcare-org audience

**Negative / accepted tradeoffs:**
- No offline viewing; every app open re-fetches from the FHIR sandbox
- Slightly worse perceived performance/UX than a cached app — accepted deliberately, not an oversight

**Risks:**
- If a refresh token is ever persisted incorrectly (e.g. accidentally logged, or stored outside the Keystore-backed encrypted store), that undoes this ADR's entire point — the implementation must be reviewed specifically against this before merging any auth code
- Not yet implemented — see `docs/RISKS.md`, this remains a stated intent until the actual auth/data layer is built

## Notes

- If offline viewing is ever wanted, the correct solution is short-TTL encrypted local storage with explicit user consent and a visible "cached data" indicator — not silent unencrypted caching. That would need its own ADR, not a quiet amendment to this one.
