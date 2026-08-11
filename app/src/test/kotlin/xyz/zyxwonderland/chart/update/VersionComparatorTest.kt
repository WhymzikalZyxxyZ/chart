package xyz.zyxwonderland.chart.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionComparatorTest {

    @Test
    fun `newer patch version is detected`() {
        assertTrue(VersionComparator.isNewer("0.1.0", "0.1.1"))
    }

    @Test
    fun `newer minor version with v prefix is detected`() {
        assertTrue(VersionComparator.isNewer("0.1.0", "v0.2.0"))
    }

    @Test
    fun `equal versions are not newer`() {
        assertFalse(VersionComparator.isNewer("0.1.0", "0.1.0"))
        assertFalse(VersionComparator.isNewer("v0.1.0", "0.1.0"))
    }

    @Test
    fun `older candidate is not newer`() {
        assertFalse(VersionComparator.isNewer("0.2.0", "0.1.9"))
    }

    @Test
    fun `major version bump is detected even with lower minor patch`() {
        assertTrue(VersionComparator.isNewer("0.9.9", "1.0.0"))
    }

    @Test
    fun `malformed candidate tag is treated as not newer`() {
        assertFalse(VersionComparator.isNewer("0.1.0", ""))
    }

    @Test
    fun `missing components default to zero`() {
        assertFalse(VersionComparator.isNewer("1.0.0", "v1"))
        assertTrue(VersionComparator.isNewer("0.9.0", "v1"))
    }
}
