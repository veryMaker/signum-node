package brs.http

import brs.BurstException
import com.google.gson.JsonElement

class ParameterException(val errorResponse: JsonElement) : BurstException()
