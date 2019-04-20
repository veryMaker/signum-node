package brs.fluxcapacitor;

import java.util.Map;
import java.util.concurrent.Callable;

public class FluxValue<T> {
    private final T defaultValue;
    private final ValueChange<T>[] valueChanges;
    private Map<Integer, T> valuesAfterHeights;

    @SafeVarargs
    public FluxValue(T defaultValue, ValueChange<T>... valueChanges) {
        this.defaultValue = defaultValue;
        this.valueChanges = valueChanges;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public ValueChange<T>[] getValueChanges() {
        return valueChanges;
    }

    public Map<Integer, T> getValuesAfterHeights(Callable<Map<Integer, T>> creator) {
        if (valuesAfterHeights == null) {
            try {
                valuesAfterHeights = creator.call();
            } catch (Exception ignored) {
            }
        }
        return valuesAfterHeights;
    }

    public static class ValueChange<T> {
        private final HistoricalMoments historicalMoment;
        private final T newValue;

        public ValueChange(HistoricalMoments historicalMoment, T newValue) {
            this.historicalMoment = historicalMoment;
            this.newValue = newValue;
        }

        public HistoricalMoments getHistoricalMoment() {
            return historicalMoment;
        }

        public T getNewValue() {
            return newValue;
        }
    }
}
