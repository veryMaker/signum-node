package brs.http

import brs.*
import brs.util.Convert
import brs.util.toHexString
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

internal class GetMiningInfo(private val dp: DependencyProvider) : APIServlet.JsonRequestHandler(arrayOf(APITag.MINING, APITag.INFO)) {

    internal override fun processRequest(request: HttpServletRequest): JsonElement {
        val response = JsonObject()

        response.addProperty("height", (dp.blockchain.height.toLong() + 1).toString())

        val lastBlock = dp.blockchain.lastBlock
        val newGenSig = dp.generator.calculateGenerationSignature(lastBlock.generationSignature, lastBlock.generatorId)

        response.addProperty("generationSignature", newGenSig.toHexString())
        response.addProperty("baseTarget", lastBlock.baseTarget.toString())

        return response
    }
}
