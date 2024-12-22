package brs.web.api.http.handler;

import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.LegacyDocTag;

abstract class AbstractGetUnconfirmedTransactions extends ApiServlet.JsonRequestHandler {

  AbstractGetUnconfirmedTransactions(LegacyDocTag[] legacyDocTags, String... parameters) {
    super(legacyDocTags, parameters);
  }
}
