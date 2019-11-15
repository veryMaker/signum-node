package brs.api.http

import brs.util.BurstException
import com.google.gson.JsonElement

class ParameterException(val errorResponse: JsonElement) : BurstException()
