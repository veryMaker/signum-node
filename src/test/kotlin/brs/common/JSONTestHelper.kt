package brs.common

import brs.api.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.util.json.mustGetAsJsonObject
import brs.util.json.mustGetMemberAsInt
import com.google.gson.JsonElement

object JSONTestHelper {
    fun errorCode(json: JsonElement): Int {
        return json.mustGetAsJsonObject("json").mustGetMemberAsInt(ERROR_CODE_RESPONSE)
    }
}
