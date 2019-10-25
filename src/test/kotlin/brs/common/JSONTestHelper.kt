package brs.common

import brs.api.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.util.mustGetAsInt
import brs.util.mustGetAsJsonObject
import com.google.gson.JsonElement

object JSONTestHelper {

    fun errorCode(json: JsonElement): Int {
        return json.mustGetAsJsonObject("json").get(ERROR_CODE_RESPONSE).mustGetAsInt(ERROR_CODE_RESPONSE)
    }
}
