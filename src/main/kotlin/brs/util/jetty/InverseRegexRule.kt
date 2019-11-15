package brs.util.jetty

import org.eclipse.jetty.rewrite.handler.Rule
import java.io.IOException
import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

abstract class InverseRegexRule protected constructor(pattern: String) : Rule() {
    private val _regex: Pattern = Pattern.compile(pattern)

    @Throws(IOException::class)
    override fun matchAndApply(target: String, request: HttpServletRequest, response: HttpServletResponse): String? {
        val matcher = _regex.matcher(target)
        val matches = matcher.matches()
        return if (!matches) apply(target, request, response) else null
    }

    protected abstract fun apply(
        target: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): String?

    override fun toString(): String {
        return super.toString() + "[" + _regex + "]"
    }
}
