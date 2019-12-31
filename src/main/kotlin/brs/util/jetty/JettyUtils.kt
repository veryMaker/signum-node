package brs.util.jetty

import javax.servlet.http.HttpServletRequest

@Suppress("NOTHING_TO_INLINE")
inline operator fun HttpServletRequest.get(parameterName: String): String? {
    return this.getParameter(parameterName)
}
