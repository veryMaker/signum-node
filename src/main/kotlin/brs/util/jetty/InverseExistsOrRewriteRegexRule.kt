package brs.util.jetty

import org.eclipse.jetty.rewrite.handler.Rule
import org.eclipse.jetty.server.Request
import java.io.File
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class InverseExistsOrRewriteRegexRule constructor(private val baseDirectory: File, regex: String, replacement: String) : InverseRegexRule(regex), Rule.ApplyURI {
    private var replacement: String
    private var query: String? = null
    private var queryGroup: Boolean = false

    init {
        isHandling = false
        isTerminating = false
        val split = replacement.split("\\?".toRegex(), 2).toTypedArray()
        this.replacement = split[0]
        query = if (split.size == 2) split[1] else null
        queryGroup = query != null && query!!.contains("\$Q")
    }

    public override fun apply(
        target: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): String? {
        if (File(baseDirectory, target).exists()) return null
        var query = this.query
        if (query != null) {
            if (queryGroup)
                query = query.replace("\$Q", if (request.queryString == null) "" else request.queryString)
            request.setAttribute("org.eclipse.jetty.rewrite.handler.RewriteRegexRule.Q", query)
        }

        return replacement
    }

    override fun applyURI(request: Request, oldURI: String, newURI: String) {
        if (query == null) {
            request.setURIPathQuery(newURI)
        } else {
            var query = request.getAttribute("org.eclipse.jetty.rewrite.handler.RewriteRegexRule.Q") as String

            if (!queryGroup && request.queryString != null)
                query = request.queryString + "&" + query
            request.setURIPathQuery(newURI)
            request.queryString = query
        }
    }

    override fun toString(): String {
        return super.toString() + "[" + replacement + "]"
    }
}
