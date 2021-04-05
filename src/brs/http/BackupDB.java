package brs.http;

import static brs.http.common.Parameters.FILENAME_PARAMETER;
import static brs.http.common.ResultFields.ERROR_RESPONSE;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import brs.db.sql.Db;

final class BackupDB extends APIServlet.JsonRequestHandler {

  BackupDB() {
    super(new APITag[] {APITag.ADMIN}, FILENAME_PARAMETER);
  }

  @Override
  JsonElement processRequest(HttpServletRequest req) {

    JsonObject response = new JsonObject();
    String filename = req.getParameter(FILENAME_PARAMETER);

    if(filename == null || filename.length() == 0) {
      response.addProperty(ERROR_RESPONSE, "invalid filename");
      return response;
    }
    
    Db.backup(filename);
    
    return response;
  }

}
