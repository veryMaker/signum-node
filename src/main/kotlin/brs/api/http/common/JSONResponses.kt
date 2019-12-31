package brs.api.http.common

import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.ALIAS_NAME_PARAMETER
import brs.api.http.common.Parameters.ALIAS_PARAMETER
import brs.api.http.common.Parameters.AMOUNT_PARAMETER
import brs.api.http.common.Parameters.AMOUNT_PLANCK_PARAMETER
import brs.api.http.common.Parameters.ASSETS_PARAMETER
import brs.api.http.common.Parameters.ASSET_PARAMETER
import brs.api.http.common.Parameters.AT_PARAMETER
import brs.api.http.common.Parameters.BLOCK_PARAMETER
import brs.api.http.common.Parameters.CODE_PARAMETER
import brs.api.http.common.Parameters.DEADLINE_PARAMETER
import brs.api.http.common.Parameters.DECIMALS_PARAMETER
import brs.api.http.common.Parameters.DELIVERY_DEADLINE_TIMESTAMP_PARAMETER
import brs.api.http.common.Parameters.DELTA_QUANTITY_PARAMETER
import brs.api.http.common.Parameters.DESCRIPTION_PARAMETER
import brs.api.http.common.Parameters.DISCOUNT_PLANCK_PARAMETER
import brs.api.http.common.Parameters.DOMAIN_PARAMETER
import brs.api.http.common.Parameters.ENCRYPTED_MESSAGE_DATA_PARAMETER
import brs.api.http.common.Parameters.ESCROW_DEADLINE_PARAMETER
import brs.api.http.common.Parameters.FEE_PARAMETER
import brs.api.http.common.Parameters.FEE_PLANCK_PARAMETER
import brs.api.http.common.Parameters.FEE_SUGGESTION_TYPE_PARAMETER
import brs.api.http.common.Parameters.GOODS_DATA_PARAMETER
import brs.api.http.common.Parameters.GOODS_PARAMETER
import brs.api.http.common.Parameters.GOODS_TO_ENCRYPT_PARAMETER
import brs.api.http.common.Parameters.HEIGHT_PARAMETER
import brs.api.http.common.Parameters.HEX_STRING_PARAMETER
import brs.api.http.common.Parameters.ID_PARAMETER
import brs.api.http.common.Parameters.MESSAGE_PARAMETER
import brs.api.http.common.Parameters.MESSAGE_TO_ENCRYPT_PARAMETER
import brs.api.http.common.Parameters.NAME_PARAMETER
import brs.api.http.common.Parameters.NUMBER_OF_CONFIRMATIONS_PARAMETER
import brs.api.http.common.Parameters.ORDER_PARAMETER
import brs.api.http.common.Parameters.PAYLOAD_PARAMETER
import brs.api.http.common.Parameters.PEER_PARAMETER
import brs.api.http.common.Parameters.PRICE_PARAMETER
import brs.api.http.common.Parameters.PRICE_PLANCK_PARAMETER
import brs.api.http.common.Parameters.PUBLIC_KEY_PARAMETER
import brs.api.http.common.Parameters.PURCHASE_PARAMETER
import brs.api.http.common.Parameters.QUANTITY_PARAMETER
import brs.api.http.common.Parameters.QUANTITY_QNT_PARAMETER
import brs.api.http.common.Parameters.RECEIVER_ID_PARAMETER
import brs.api.http.common.Parameters.RECIPIENT_PARAMETER
import brs.api.http.common.Parameters.REFERENCED_TRANSACTION_FULL_HASH_PARAMETER
import brs.api.http.common.Parameters.REFUND_PLANCK_PARAMETER
import brs.api.http.common.Parameters.REQUIRED_SIGNERS_PARAMETER
import brs.api.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.api.http.common.Parameters.SELLER_PARAMETER
import brs.api.http.common.Parameters.SIGNATURE_HASH_PARAMETER
import brs.api.http.common.Parameters.TAGS_PARAMETER
import brs.api.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.api.http.common.Parameters.TRANSACTION_BYTES_PARAMETER
import brs.api.http.common.Parameters.TRANSACTION_JSON_PARAMETER
import brs.api.http.common.Parameters.TRANSACTION_PARAMETER
import brs.api.http.common.Parameters.UNSIGNED_TRANSACTION_BYTES_PARAMETER
import brs.api.http.common.Parameters.URI_PARAMETER
import brs.api.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.api.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE
import brs.objects.Constants
import com.google.gson.JsonElement
import com.google.gson.JsonObject

object JSONResponses {
    val ALREADY_DELIVERED = error(8, "Purchase already delivered")
    val DECRYPTION_FAILED = error(8, "Decryption failed")
    val DUPLICATE_REFUND = error(8, "Refund already sent")
    val ERROR_INCORRECT_REQUEST = error(1, "Incorrect request")
    val ERROR_MISSING_REQUEST = error(1, "Request type does not exist")
    val ERROR_NOT_ALLOWED = error(7, "Not allowed")
    val FEATURE_NOT_AVAILABLE = error(9, "Feature not available")
    val FEE_OR_FEE_SUGGESTION_REQUIRED = incorrect(FEE_SUGGESTION_TYPE_PARAMETER, "Either feeNQT or feeSuggestionType is a required parameter")
    val FEE_SUGGESTION_TYPE_INVALID = incorrect(FEE_SUGGESTION_TYPE_PARAMETER, "feeSuggestionType is not valid")
    val GOODS_NOT_DELIVERED = error(8, "Goods have not been delivered")
    val HEIGHT_NOT_AVAILABLE = error(8, "Requested height not available")
    val INCORRECT_ACCOUNT = incorrect(ACCOUNT_PARAMETER)
    val INCORRECT_ACCOUNT_DESCRIPTION_LENGTH = incorrect(DESCRIPTION_PARAMETER, "(length must be less than ${Constants.MAX_ACCOUNT_DESCRIPTION_LENGTH} characters)")
    val INCORRECT_ACCOUNT_NAME_LENGTH = incorrect(NAME_PARAMETER, "(length must be less than ${Constants.MAX_ACCOUNT_NAME_LENGTH} characters)")
    val INCORRECT_ALIAS = incorrect(ALIAS_PARAMETER)
    val INCORRECT_ALIAS_LENGTH = incorrect(ALIAS_PARAMETER, "(length must be in range [1..${Constants.MAX_ALIAS_LENGTH}])")
    val INCORRECT_ALIAS_NAME = incorrect(ALIAS_PARAMETER, "(must contain only digits and latin letters)")
    val INCORRECT_ALIAS_NOTFORSALE = incorrect(ALIAS_PARAMETER, "(alias is not for sale at the moment)")
    val INCORRECT_ALIAS_OWNER = incorrect(ALIAS_PARAMETER, "(invalid alias owner)")
    val INCORRECT_AMOUNT = incorrect(AMOUNT_PARAMETER)
    val INCORRECT_ARBITRARY_MESSAGE = incorrect(MESSAGE_PARAMETER)
    val INCORRECT_ASSET = incorrect(ASSET_PARAMETER)
    val INCORRECT_ASSET_DESCRIPTION = incorrect(DESCRIPTION_PARAMETER, "(length must not exceed ${Constants.MAX_ASSET_DESCRIPTION_LENGTH} characters)")
    val INCORRECT_ASSET_NAME = incorrect(NAME_PARAMETER, "(must contain only digits and latin letters)")
    val INCORRECT_ASSET_NAME_LENGTH = incorrect(NAME_PARAMETER, "(length must be in [" + Constants.MIN_ASSET_NAME_LENGTH + ".." + Constants.MAX_ASSET_NAME_LENGTH + "] range)")
    val INCORRECT_ASSET_QUANTITY = incorrect(QUANTITY_PARAMETER, "(must be in [1.." + Constants.MAX_ASSET_QUANTITY + "] range)")
    val INCORRECT_AT = incorrect(AT_PARAMETER)
    val INCORRECT_AUTOMATED_TRANSACTION_DESCRIPTION = incorrect(DESCRIPTION_PARAMETER, "(length must not exceed " + Constants.MAX_AUTOMATED_TRANSACTION_DESCRIPTION_LENGTH + " characters)")
    val INCORRECT_AUTOMATED_TRANSACTION_NAME = incorrect(NAME_PARAMETER, "(must contain only digits and latin letters)")
    val INCORRECT_AUTOMATED_TRANSACTION_NAME_LENGTH = incorrect(DESCRIPTION_PARAMETER, "(length must not exceed " + Constants.MAX_AUTOMATED_TRANSACTION_NAME_LENGTH + " characters)")
    val INCORRECT_BLOCK = incorrect(BLOCK_PARAMETER)
    val INCORRECT_CREATION_BYTES = incorrect("incorrect creation bytes")
    val INCORRECT_DEADLINE = incorrect(DEADLINE_PARAMETER)
    val INCORRECT_DECIMALS = incorrect(DECIMALS_PARAMETER)
    val INCORRECT_DELIVERY_DEADLINE_TIMESTAMP = incorrect(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER)
    val INCORRECT_DELTA_QUANTITY = incorrect(DELTA_QUANTITY_PARAMETER)
    val INCORRECT_DGS_DISCOUNT = incorrect(DISCOUNT_PLANCK_PARAMETER)
    val INCORRECT_DGS_ENCRYPTED_GOODS = incorrect(GOODS_DATA_PARAMETER)
    val INCORRECT_DGS_GOODS = incorrect(GOODS_TO_ENCRYPT_PARAMETER)
    val INCORRECT_DGS_LISTING_DESCRIPTION = incorrect(DESCRIPTION_PARAMETER, "(length must be not longer than ${Constants.MAX_DGS_LISTING_DESCRIPTION_LENGTH} characters)")
    val INCORRECT_DGS_LISTING_NAME = incorrect(NAME_PARAMETER, "(length must be not longer than ${Constants.MAX_DGS_LISTING_NAME_LENGTH} characters)")
    val INCORRECT_DGS_LISTING_TAGS = incorrect(TAGS_PARAMETER, "(length must be not longer than ${Constants.MAX_DGS_LISTING_TAGS_LENGTH} characters)")
    val INCORRECT_DGS_REFUND = incorrect(REFUND_PLANCK_PARAMETER)
    val INCORRECT_ENCRYPTED_MESSAGE = incorrect(ENCRYPTED_MESSAGE_DATA_PARAMETER)
    val INCORRECT_FEE = incorrect(FEE_PARAMETER)
    val INCORRECT_GOODS = incorrect(GOODS_PARAMETER)
    val INCORRECT_HEIGHT = incorrect(HEIGHT_PARAMETER)
    val INCORRECT_MESSAGE_LENGTH = incorrect(MESSAGE_PARAMETER, "Message can have a max length of " + Constants.MAX_ARBITRARY_MESSAGE_LENGTH)
    val INCORRECT_NUMBER_OF_CONFIRMATIONS = incorrect(NUMBER_OF_CONFIRMATIONS_PARAMETER)
    val INCORRECT_ORDER = incorrect(ORDER_PARAMETER)
    val INCORRECT_PLAIN_MESSAGE = incorrect(MESSAGE_TO_ENCRYPT_PARAMETER)
    val INCORRECT_PRICE = incorrect(PRICE_PARAMETER)
    val INCORRECT_PUBLIC_KEY = incorrect(PUBLIC_KEY_PARAMETER)
    val INCORRECT_PURCHASE = incorrect(PURCHASE_PARAMETER)
    val INCORRECT_PURCHASE_PRICE = incorrect(PRICE_PLANCK_PARAMETER, "(purchase price doesn't match goods price)")
    val INCORRECT_PURCHASE_QUANTITY = incorrect(QUANTITY_PARAMETER, "(quantity exceeds available goods quantity)")
    val INCORRECT_QUANTITY = incorrect(QUANTITY_PARAMETER)
    val INCORRECT_RECIPIENT = incorrect(RECIPIENT_PARAMETER)
    val INCORRECT_REFERENCED_TRANSACTION = incorrect(REFERENCED_TRANSACTION_FULL_HASH_PARAMETER)
    val INCORRECT_TIMESTAMP = incorrect(TIMESTAMP_PARAMETER)
    val INCORRECT_TRANSACTION = incorrect(TRANSACTION_PARAMETER)
    val INCORRECT_URI_LENGTH = incorrect(URI_PARAMETER, "(length must be not longer than ${Constants.MAX_ALIAS_URI_LENGTH} characters)")
    val MISSING_ACCOUNT = missing(ACCOUNT_PARAMETER)
    val MISSING_ALIAS_NAME = missing(ALIAS_NAME_PARAMETER)
    val MISSING_ALIAS_OR_ALIAS_NAME = missing(ALIAS_PARAMETER, ALIAS_NAME_PARAMETER)
    val MISSING_AMOUNT = missing(AMOUNT_PLANCK_PARAMETER)
    val MISSING_ASSET = missing(ASSET_PARAMETER)
    val MISSING_ASSETS = missing(ASSETS_PARAMETER)
    val MISSING_AT = missing(AT_PARAMETER)
    val MISSING_CODE = missing(CODE_PARAMETER)
    val MISSING_DEADLINE = missing(DEADLINE_PARAMETER)
    val MISSING_DELIVERY_DEADLINE_TIMESTAMP = missing(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER)
    val MISSING_DELTA_QUANTITY = missing(DELTA_QUANTITY_PARAMETER)
    val MISSING_DOMAIN = missing(DOMAIN_PARAMETER)
    val MISSING_ESCROW_DEADLINE = missing(ESCROW_DEADLINE_PARAMETER)
    val MISSING_FEE = missing(FEE_PLANCK_PARAMETER)
    val MISSING_GOODS = missing(GOODS_PARAMETER)
    val MISSING_HEIGHT = missing(HEIGHT_PARAMETER)
    val MISSING_HEX_STRING = missing(HEX_STRING_PARAMETER)
    val MISSING_ID = missing(ID_PARAMETER)
    val MISSING_NAME = missing(NAME_PARAMETER)
    val MISSING_ORDER = missing(ORDER_PARAMETER)
    val MISSING_PEER = missing(PEER_PARAMETER)
    val MISSING_PRICE = missing(PRICE_PLANCK_PARAMETER)
    val MISSING_PURCHASE = missing(PURCHASE_PARAMETER)
    val MISSING_QUANTITY = missing(QUANTITY_QNT_PARAMETER)
    val MISSING_RECEIVER_ID = missing(RECEIVER_ID_PARAMETER)
    val MISSING_RECIPIENT = missing(RECIPIENT_PARAMETER)
    val MISSING_REQUIRED_SIGNERS = missing(REQUIRED_SIGNERS_PARAMETER)
    val MISSING_SECRET_PHRASE = missing(SECRET_PHRASE_PARAMETER)
    val MISSING_SECRET_PHRASE_OR_PUBLIC_KEY = missing(SECRET_PHRASE_PARAMETER, PUBLIC_KEY_PARAMETER)
    val MISSING_SELLER = missing(SELLER_PARAMETER)
    val MISSING_SIGNATURE_HASH = missing(SIGNATURE_HASH_PARAMETER)
    val MISSING_TRANSACTION = missing(TRANSACTION_PARAMETER)
    val MISSING_TRANSACTION_BYTES_OR_JSON = missing(TRANSACTION_BYTES_PARAMETER, TRANSACTION_JSON_PARAMETER)
    val MISSING_UNSIGNED_BYTES = missing(UNSIGNED_TRANSACTION_BYTES_PARAMETER)
    val NO_MESSAGE = error(8, "No attached message found")
    val NOT_ENOUGH_ASSETS = error(6, "Not enough assets")
    val NOT_ENOUGH_FUNDS = error(6, "Not enough funds")
    val PAYLOAD_WITHOUT_ACTION = incorrect(PAYLOAD_PARAMETER, "With 'payload' parameter the 'action' parameter is mandatory")
    val POST_REQUIRED = error(1, "This request is only accepted using POST")
    val UNKNOWN_ACCOUNT = unknown(ACCOUNT_PARAMETER)
    val UNKNOWN_ALIAS = unknown(ALIAS_PARAMETER)
    val UNKNOWN_ASSET = unknown(ASSET_PARAMETER)
    val UNKNOWN_AT = unknown(AT_PARAMETER)
    val UNKNOWN_BLOCK = unknown(BLOCK_PARAMETER)
    val UNKNOWN_GOODS = unknown(GOODS_PARAMETER)
    val UNKNOWN_ORDER = unknown(ORDER_PARAMETER)
    val UNKNOWN_PEER = unknown(PEER_PARAMETER)
    val UNKNOWN_TRANSACTION = unknown(TRANSACTION_PARAMETER)

    private fun missing(param: String) = error(3, "\"$param\" not specified")
    private fun missing(vararg paramNames: String) = error(3, "At least one of ${paramNames.contentToString()} must be specified")
    fun incorrect(paramName: String, details: String = "") = error(4, "Incorrect \"$paramName\"${if (details.isBlank()) "" else ": $details"}")
    private fun unknown(objectName: String) = error(5, "Unknown $objectName")
    fun unknownParameter(paramName: String) = error(5, "Parameter \"$paramName\" not expected")

    private fun error(errorCode: Int, errorDescription: String): JsonElement {
        val response = JsonObject()
        response.addProperty(ERROR_CODE_RESPONSE, errorCode)
        response.addProperty(ERROR_DESCRIPTION_RESPONSE, errorDescription)
        return response
    }
}
