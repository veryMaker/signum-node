package brs.fluxcapacitor;

import java.util.Arrays;
import java.util.List;

public class FluxValue<T> {
    private final T defaultValue;
    private List<ValueChange<T>> valueChanges;

    @SafeVarargs
    public FluxValue(T defaultValue, ValueChange<T>... valueChanges) {
        this.defaultValue = defaultValue;
        this.valueChanges = Arrays.asList(valueChanges);
    }
    
    public void updateValueChanges(List<ValueChange<T>> valueChanges) {
        this.valueChanges = valueChanges;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public List<ValueChange<T>> getValueChanges() {
        return valueChanges;
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
