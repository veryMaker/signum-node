package brs.fluxcapacitor;

import brs.Blockchain;
import brs.props.PropertyService;
import brs.props.Props;

import java.util.HashMap;
import java.util.Map;

public class FluxCapacitorImpl implements FluxCapacitor {

    private final PropertyService propertyService;
    private final Blockchain blockchain;

    public FluxCapacitorImpl(Blockchain blockchain, PropertyService propertyService) {
        this.propertyService = propertyService;
        this.blockchain = blockchain;
    }

    @Override
    public <T> T getValue(FluxValue<T> fluxValue) {
        return getValueAt(fluxValue, blockchain.getHeight());
    }

    @Override
    public <T> T getValue(FluxValue<T> fluxValue, int height) {
        return getValueAt(fluxValue, height);
    }

    private int getHistoricalMomentHeight(HistoricalMoments historicalMoment) {
        if (propertyService.getBoolean(Props.DEV_TESTNET)) {
            int overridingHeight = propertyService.getInt(historicalMoment.getOverridingProperty());
            return overridingHeight < 0 ? historicalMoment.getTestnetHeight() : overridingHeight;
        } else {
            return historicalMoment.getMainnetHeight();
        }
    }

    private <T> Map<Integer, T> computeValuesAtHeights(FluxValue<T> fluxValue) {
        Map<Integer, T> valuesAfterHeights = new HashMap<>();
        for (FluxValue.ValueChange<T> valueChange : fluxValue.getValueChanges()) {
            valuesAfterHeights.put(getHistoricalMomentHeight(valueChange.getHistoricalMoment()), valueChange.getNewValue());
        }
        return valuesAfterHeights;
    }

    private <T> T getValueAt(FluxValue<T> fluxValue, int height) {
        T mostRecentValue = fluxValue.getDefaultValue();
        int mostRecentChangeHeight = 0;
        for (Map.Entry<Integer, T> entry : fluxValue.getValuesAfterHeights(() -> computeValuesAtHeights(fluxValue)).entrySet()) {
            int entryHeight = entry.getKey();
            if (entryHeight <= height && entryHeight > mostRecentChangeHeight) {
                mostRecentValue = entry.getValue();
                mostRecentChangeHeight = entryHeight;
            }
        }
        return mostRecentValue;
    }

    @Override
    public Integer getStartingHeight(FluxEnable fluxEnable) {
        return getHistoricalMomentHeight(fluxEnable.getEnablePoint());
    }
}
