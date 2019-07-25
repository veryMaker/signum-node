package brs.common

import brs.util.JSON
import com.google.gson.JsonElement

import brs.http.common.ResultFields.ERROR_CODE_RESPONSE

object JSONTestHelper {

    fun errorCode(json: JsonElement): Int {
        return JSON.getAsInt(JSON.getAsJsonObject(json).get(ERROR_CODE_RESPONSE))
    }
}
