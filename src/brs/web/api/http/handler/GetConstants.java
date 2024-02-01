package brs.web.api.http.handler;

import javax.servlet.http.HttpServletRequest;

import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import brs.Signum;
import brs.Constants;
import brs.Genesis;
import brs.TransactionType;
import brs.TransactionType.Fee;
import brs.fluxcapacitor.FluxValues;
import brs.props.Props;
import brs.util.Convert;
import brs.util.JSON;
import signumj.util.SignumUtils;

public final class GetConstants extends ApiServlet.JsonRequestHandler {

    static public final GetConstants instance = new GetConstants();

    private GetConstants() {
        super(new LegacyDocTag[] {LegacyDocTag.INFO});
    }

    @Override
    protected
    JsonElement processRequest(HttpServletRequest req) {
        JsonObject response = new JsonObject();
        response.addProperty("networkName", Signum.getPropertyService().getString(Props.NETWORK_NAME));
        response.addProperty("genesisBlockId", Signum.getPropertyService().getString(Props.GENESIS_BLOCK_ID));
        response.addProperty("genesisAccountId", Convert.toUnsignedLong(Genesis.CREATOR_ID));
        response.addProperty("maxBlockPayloadLength", (Signum.getFluxCapacitor().getValue(FluxValues.MAX_PAYLOAD_LENGTH)));
        response.addProperty("maxArbitraryMessageLength", Constants.MAX_ARBITRARY_MESSAGE_LENGTH);
        response.addProperty("ordinaryTransactionLength", Constants.ORDINARY_TRANSACTION_BYTES);
        response.addProperty("addressPrefix", SignumUtils.getAddressPrefix());
        response.addProperty("valueSuffix", SignumUtils.getValueSuffix());
        response.addProperty("blockTime", Signum.getFluxCapacitor().getValue(FluxValues.BLOCK_TIME));
        response.addProperty("decimalPlaces", Signum.getPropertyService().getInt(Props.DECIMAL_PLACES));
        response.addProperty("feeQuantNQT", Signum.getFluxCapacitor().getValue(FluxValues.FEE_QUANT));
        response.addProperty("cashBackId", Signum.getPropertyService().getString(Props.CASH_BACK_ID));
        response.addProperty("cashBackFactor", Signum.getPropertyService().getInt(Props.CASH_BACK_FACTOR));

        JsonArray transactionTypes = new JsonArray();
        TransactionType.getTransactionTypes()
                .forEach((key, value) -> {
                    JsonObject transactionType = new JsonObject();
                    transactionType.addProperty("value", key.getType());
                    transactionType.addProperty("description", key.getDescription());
                    JsonArray transactionSubtypes = new JsonArray();
                    transactionSubtypes.addAll(value.entrySet().stream()
                            .map(entry -> {
                                JsonObject transactionSubtype = new JsonObject();
                                Fee fee = entry.getValue().getBaselineFee(Signum.getBlockchain().getHeight());
                                transactionSubtype.addProperty("value", entry.getKey());
                                transactionSubtype.addProperty("description", entry.getValue().getDescription());
                                transactionSubtype.addProperty("minimumFeeConstantNQT", fee.getConstantFee());
                                transactionSubtype.addProperty("minimumFeeAppendagesNQT", fee.getAppendagesFee());
                                return transactionSubtype;
                            })
                            .collect(JSON.jsonArrayCollector()));
                    transactionType.add("subtypes", transactionSubtypes);
                    transactionTypes.add(transactionType);
                });
        response.add("transactionTypes", transactionTypes);

        JsonArray peerStates = new JsonArray();
        JsonObject peerState = new JsonObject();
        peerState.addProperty("value", 0);
        peerState.addProperty("description", "Non-connected");
        peerStates.add(peerState);
        peerState = new JsonObject();
        peerState.addProperty("value", 1);
        peerState.addProperty("description", "Connected");
        peerStates.add(peerState);
        peerState = new JsonObject();
        peerState.addProperty("value", 2);
        peerState.addProperty("description", "Disconnected");
        peerStates.add(peerState);
        response.add("peerStates", peerStates);

        return response;
    }
}
