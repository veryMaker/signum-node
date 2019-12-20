package brs.util

import java.util.*

class Version(
    private val major: Int,
    private val minor: Int,
    private val patch: Int,
    private val prereleaseTag: PrereleaseTag,
    private val prereleaseIteration: Int
) {
    val isPrelease: Boolean
        get() = prereleaseTag != PrereleaseTag.NONE

    override fun toString(): String {
        val baseVersion = "v$major.$minor.$patch"
        val prereleaseSuffix = "-" + prereleaseTag.tag + if (prereleaseIteration >= 0) prereleaseIteration else ""
        return if (prereleaseTag == PrereleaseTag.NONE) baseVersion else baseVersion + prereleaseSuffix
    }

    fun isGreaterThan(otherVersion: Version): Boolean {
        if (major > otherVersion.major) return true
        if (major < otherVersion.major) return false
        if (minor > otherVersion.minor) return true
        if (minor < otherVersion.minor) return false
        if (patch > otherVersion.patch) return true
        if (patch < otherVersion.patch) return false
        if (prereleaseTag.priority > otherVersion.prereleaseTag.priority) return true
        return prereleaseIteration > otherVersion.prereleaseIteration
    }

    fun isGreaterThanOrEqualTo(otherVersion: Version): Boolean {
        return isGreaterThan(otherVersion) || this == otherVersion
    }

    fun toStringIfNotEmpty(): String {
        return if (equals(EMPTY)) {
            ""
        } else {
            toString()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val version = other as Version?

        if (major != version!!.major) return false
        if (minor != version.minor) return false
        if (patch != version.patch) return false
        return prereleaseIteration == version.prereleaseIteration && prereleaseTag == version.prereleaseTag
    }

    override fun hashCode(): Int {
        var result = major
        result = 31 * result + minor
        result = 31 * result + patch
        result = 31 * result + prereleaseTag.hashCode()
        result = 31 * result + prereleaseIteration
        return result
    }

    enum class PrereleaseTag constructor(internal val tag: String) {
        DEVELOPMENT("dev"),
        ALPHA("alpha"),
        BETA("beta"),
        RC("rc"),
        NONE("");

        internal val priority: Int = ordinal

        companion object {
            fun withTag(tag: String): PrereleaseTag {
                for (prereleaseTag in values()) {
                    if (prereleaseTag.tag == tag) {
                        return prereleaseTag
                    }
                }
                throw IllegalArgumentException("Provided does not match any prelease tags")
            }
        }
    }

    companion object {
        val EMPTY = Version(0, 0, 0, PrereleaseTag.NONE, -1)

        private val prereleaseTagRegex = Regex("(?<=[a-z])(?=[0-9])")

        fun parse(versionString: String?): Version {
            if (versionString.isNullOrBlank()) {
                return EMPTY
            }
            var version = versionString
            try {
                version = version.replace("-", ".").toLowerCase(Locale.ENGLISH)
                if (version.startsWith("v")) version = version.substring(1)
                val tokenizer = StringTokenizer(version, ".", false)
                val major = Integer.parseInt(tokenizer.nextToken())
                val minor = Integer.parseInt(tokenizer.nextToken())
                val patch = Integer.parseInt(tokenizer.nextToken())
                return if (tokenizer.hasMoreTokens()) {
                    val prereleaseTagAndIteration =
                        tokenizer.nextToken().split(prereleaseTagRegex).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    val prereleaseTag = PrereleaseTag.withTag(prereleaseTagAndIteration[0])
                    val prereleaseIteration =
                        if (prereleaseTagAndIteration.size == 2) Integer.parseInt(prereleaseTagAndIteration[1]) else -1
                    Version(major, minor, patch, prereleaseTag, prereleaseIteration)
                } else {
                    Version(major, minor, patch, PrereleaseTag.NONE, -1)
                }
            } catch (e: Exception) {
                throw IllegalArgumentException("Version not formatted correctly", e)
            }
        }
    }
}
