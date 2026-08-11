package xyz.zyxwonderland.chart.update

/**
 * Minimal major.minor.patch comparator for GitHub release tags (e.g. "v0.2.0" vs. "0.1.0").
 * Deliberately not a full semver parser (no pre-release/build-metadata precedence) — CHART's
 * own release tags are simple and that's the only input this ever sees.
 */
object VersionComparator {
    fun isNewer(currentVersionName: String, candidateTag: String): Boolean {
        val current = parse(currentVersionName) ?: return false
        val candidate = parse(candidateTag) ?: return false
        for (i in 0..2) {
            if (candidate[i] != current[i]) return candidate[i] > current[i]
        }
        return false
    }

    private fun parse(raw: String): IntArray? {
        val cleaned = raw.trim().removePrefix("v").removePrefix("V")
        if (cleaned.isEmpty()) return null
        val parts = cleaned.split(".", "-", "+")
        return IntArray(3) { i -> parts.getOrNull(i)?.toIntOrNull() ?: 0 }
    }
}
