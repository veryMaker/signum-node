package brs.entity

import brs.Version
import org.junit.Test

import org.junit.Assert.*

class VersionTest {
    @Test
    fun testVersionParse() {
        val validVersions = arrayOf("2.2.2", "v1.2.3", "v0.2.7-dev", "1.24.2-rc9", "v0.8.999-beta99")

        val invalidVersions = arrayOf("v2.0", "v1.2.3-abc123", "v1a.2.3-dev", "1.0-dev", "v1.2.3-123dev123", "", null)

        validVersions.forEach { versionString ->
            val version = Version.parse(versionString)
            if (versionString.startsWith("v")) {
                assertEquals(versionString, version.toString())
            } else {
                assertEquals("v$versionString", version.toString())
            }
        }

        invalidVersions.forEach { versionString ->
            try {
                val version = Version.parse(versionString)
                if (version === Version.EMPTY) throw IllegalArgumentException()
                throw AssertionError("Did not fail to parse: $versionString")
            } catch (ignored: IllegalArgumentException) {
            }

        }
    }

    @Test
    fun testVersionCompare() {
        val lower = Version.parse("v1.0.0")
        val equal = Version.parse("v1.0.0")
        val higher = Version.parse("v1.0.1")
        val higherPreRelease = Version.parse("v1.5.0-dev")

        assertFalse(lower.isGreaterThan(equal))
        assertTrue(lower.isGreaterThanOrEqualTo(equal))
        assertFalse(lower.isGreaterThanOrEqualTo(higher))
        assertTrue(higher.isGreaterThan(lower))
        assertFalse(higher.isGreaterThanOrEqualTo(higherPreRelease))
        assertTrue(higherPreRelease.isGreaterThan(higher))
        assertFalse(higher.isPrelease)
        assertTrue(higherPreRelease.isPrelease)
        assertTrue(higherPreRelease.isGreaterThanOrEqualTo(higherPreRelease))
    }
}
