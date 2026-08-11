package xyz.zyxwonderland.chart.update

import android.content.SharedPreferences
import androidx.core.content.edit
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

private const val LATEST_RELEASE_URL = "https://api.github.com/repos/WhymzikalZyxxyZ/chart/releases/latest"
private const val CHECK_INTERVAL_MS = 24 * 60 * 60 * 1000L

private const val KEY_LAST_CHECK = "update_last_check_ms"
private const val KEY_LATEST_TAG = "update_latest_tag"
private const val KEY_LATEST_URL = "update_latest_url"
private const val KEY_DISMISSED_TAG = "update_dismissed_tag"

/**
 * Checks this app's own GitHub Releases for a newer tag than the running build.
 * There's no Play Store to auto-update a sideloaded APK, so this is the app's only
 * update signal — see docs/adr/001-platform-and-distribution.md.
 */
class UpdateChecker(
    private val client: HttpClient,
    private val prefs: SharedPreferences,
) {
    suspend fun checkForUpdate(currentVersionName: String): UpdateInfo? {
        val cached = cachedUpdateInfo()
        if (!shouldCheckNow()) return cached

        return try {
            val release: GitHubRelease = client.get(LATEST_RELEASE_URL) {
                // GitHub's API rejects requests with no User-Agent.
                header(HttpHeaders.UserAgent, "CHART-Android")
            }.body()
            prefs.edit { putLong(KEY_LAST_CHECK, System.currentTimeMillis()) }

            if (release.draft || release.prerelease || !VersionComparator.isNewer(currentVersionName, release.tagName)) {
                prefs.edit { remove(KEY_LATEST_TAG); remove(KEY_LATEST_URL) }
                return null
            }

            prefs.edit {
                putString(KEY_LATEST_TAG, release.tagName)
                putString(KEY_LATEST_URL, release.htmlUrl)
            }
            cachedUpdateInfo()
        } catch (e: Exception) {
            // Best-effort, non-critical: offline, GitHub down/rate-limited, or no release
            // exists yet (404 on /releases/latest for a repo with zero releases). Never let
            // this crash app startup — fall back to whatever we last knew, if anything.
            cached
        }
    }

    fun dismiss(versionTag: String) {
        prefs.edit { putString(KEY_DISMISSED_TAG, versionTag) }
    }

    private fun shouldCheckNow(): Boolean {
        val last = prefs.getLong(KEY_LAST_CHECK, 0L)
        return System.currentTimeMillis() - last > CHECK_INTERVAL_MS
    }

    private fun cachedUpdateInfo(): UpdateInfo? {
        val tag = prefs.getString(KEY_LATEST_TAG, null) ?: return null
        val url = prefs.getString(KEY_LATEST_URL, null) ?: return null
        if (prefs.getString(KEY_DISMISSED_TAG, null) == tag) return null
        return UpdateInfo(tag, url)
    }
}
