package brs.api.http

import brs.entity.DependencyProvider
import brs.util.convert.toHexString
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetMiningInfo(private val dp: DependencyProvider) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.MINING, APITag.INFO)) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val response = JsonObject()

        response.addProperty("height", (dp.blockchainService.height.toLong() + 1).toString())

        val lastBlock = dp.blockchainService.lastBlock
        val newGenSig =
            dp.generatorService.calculateGenerationSignature(lastBlock.generationSignature, lastBlock.generatorId)

        response.addProperty("generationSignature", newGenSig.toHexString())
        response.addProperty("baseTarget", lastBlock.baseTarget.toString())

        return response
    }
}
