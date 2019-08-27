package brs.http

import brs.*
import brs.fluxcapacitor.FluxValues
import brs.util.Convert
import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.util.stream.Collector

import javax.servlet.http.HttpServletRequest

internal class GetConstants(dp: DependencyProvider) : APIServlet.JsonRequestHandler(arrayOf(APITag.INFO)) {

    private val constants: JsonElement

    init {
        val response = JsonObject()
        response.addProperty("genesisBlockId", Convert.toUnsignedLong(Genesis.GENESIS_BLOCK_ID))
        response.addProperty("genesisAccountId", Convert.toUnsignedLong(Genesis.CREATOR_ID))
        response.addProperty("maxBlockPayloadLength", dp.fluxCapacitor.getValue(FluxValues.MAX_PAYLOAD_LENGTH))
        response.addProperty("maxArbitraryMessageLength", Constants.MAX_ARBITRARY_MESSAGE_LENGTH)

        val transactionTypes = JsonArray()
        TransactionType.getTransactionTypes()
                .forEach { (key, value) ->
                    val transactionType = JsonObject()
                    transactionType.addProperty("value", key)
                    transactionType.addProperty("description", TransactionType.getTypeDescription(key!!))
                    val transactionSubtypes = JsonArray()
                    transactionSubtypes.addAll(value.entries.stream()
                            .map { entry ->
                                val transactionSubtype = JsonObject()
                                transactionSubtype.addProperty("value", entry.key)
                                transactionSubtype.addProperty("description", entry.value.description)
                                transactionSubtype
                            }
                            .collect(JSON.jsonArrayCollector<JsonObject>() as Collector<JsonObject, Any, JsonArray>)) // TODO unchecked cast
                    transactionType.add("subtypes", transactionSubtypes)
                    transactionTypes.add(transactionType)
                }
        response.add("transactionTypes", transactionTypes)

        val peerStates = JsonArray()
        var peerState = JsonObject()
        peerState.addProperty("value", 0)
        peerState.addProperty("description", "Non-connected")
        peerStates.add(peerState)
        peerState = JsonObject()
        peerState.addProperty("value", 1)
        peerState.addProperty("description", "Connected")
        peerStates.add(peerState)
        peerState = JsonObject()
        peerState.addProperty("value", 2)
        peerState.addProperty("description", "Disconnected")
        peerStates.add(peerState)
        response.add("peerStates", peerStates)

        // TODO remove this always empty field
        response.add("requestTypes", JsonObject())

        constants = response
    }

    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        return constants
    }
}
