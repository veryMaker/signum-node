package brs.web.api.http.handler;

import brs.Asset;
import brs.Signum;
import brs.BurstException;
import brs.assetexchange.AssetExchange;
import brs.services.AccountService;
import brs.util.CollectionWithIndex;
import brs.util.Convert;
import brs.util.TextUtils;

import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.INCORRECT_ASSET_NAME;
import static brs.web.api.http.common.JSONResponses.MISSING_NAME;
import static brs.web.api.http.common.Parameters.*;
import static brs.web.api.http.common.ResultFields.*;

public final class GetAssetsByName extends AbstractAssetsRetrieval {

    private final AssetExchange assetExchange;

    public GetAssetsByName(AssetExchange assetExchange, AccountService accountService) {
        super(new LegacyDocTag[] {LegacyDocTag.ACCOUNTS}, assetExchange, accountService, NAME_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER,
           HEIGHT_START_PARAMETER, HEIGHT_END_PARAMETER, SKIP_ZERO_VOLUME_PARAMETER);
        this.assetExchange = assetExchange;
    }

    @Override
    protected
    JsonElement processRequest(HttpServletRequest req) throws BurstException {

        String name = req.getParameter(NAME_PARAMETER);

        if (name == null) {
          return MISSING_NAME;
        }
        name = name.trim();

        if (name.length() < 1 || !TextUtils.isInAlphabet(name)) {
          return INCORRECT_ASSET_NAME;
        }

        int heightEnd = Signum.getBlockchain().getHeight();
        // default is one day window
        int heightStart = heightEnd - 360;

        String heightStartString = Convert.emptyToNull(req.getParameter(HEIGHT_START_PARAMETER));
        if(heightStartString != null) {
          heightStart = Integer.parseInt(heightStartString);
        }

        String heightEndString = Convert.emptyToNull(req.getParameter(HEIGHT_END_PARAMETER));
        if(heightEndString != null) {
          heightEnd = Integer.parseInt(heightEndString);
        }

        boolean skipZeroVolume = "true".equalsIgnoreCase(req.getParameter(SKIP_ZERO_VOLUME_PARAMETER));

        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JsonObject response = new JsonObject();
        CollectionWithIndex<Asset> assets = assetExchange.getAssetsByName(name, firstIndex, lastIndex);
        response.add(ASSETS_RESPONSE, assetsToJson(assets.iterator(),
            heightStart, heightEnd, skipZeroVolume));

        if(assets.hasNextIndex()) {
          response.addProperty(NEXT_INDEX_RESPONSE, assets.nextIndex());
        }

        return response;

    }
}
