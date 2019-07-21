package brs.http

import brs.Block
import brs.Blockchain
import brs.Burst
import brs.Generator
import brs.util.Convert
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

internal class GetMiningInfo(private val blockchain: Blockchain, private val generator: Generator) : APIServlet.JsonRequestHandler(arrayOf(APITag.MINING, APITag.INFO)) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val response = JsonObject()

        response.addProperty("height", java.lang.Long.toString(Burst.getBlockchain().height.toLong() + 1))

        val lastBlock = blockchain.lastBlock
        val newGenSig = generator.calculateGenerationSignature(lastBlock.generationSignature, lastBlock.generatorId)

        response.addProperty("generationSignature", Convert.toHexString(newGenSig))
        response.addProperty("baseTarget", java.lang.Long.toString(lastBlock.baseTarget))

        return response
    }
}
