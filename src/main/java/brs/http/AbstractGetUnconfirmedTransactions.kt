package brs.http

internal abstract class AbstractGetUnconfirmedTransactions(apiTags: Array<APITag>, vararg parameters: String) : APIServlet.JsonRequestHandler(apiTags, *parameters)
