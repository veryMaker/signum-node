package brs.fluxcapacitor;

import brs.props.Prop;
import brs.props.Props;

public class HistoricalMoments {
    public static HistoricalMoments GENESIS = new HistoricalMoments(0, null);
    public static HistoricalMoments REWARD_RECIPIENT_ENABLE = new HistoricalMoments(6500, Props.REWARD_RECIPIENT_ENABLE_BLOCK_HEIGHT);
    public static HistoricalMoments DIGITAL_GOODS_STORE_BLOCK = new HistoricalMoments(11800, Props.DIGITAL_GOODS_STORE_BLOCK_HEIGHT);
    public static HistoricalMoments AUTOMATED_TRANSACTION_BLOCK = new HistoricalMoments(49200, Props.AUTOMATED_TRANSACTION_BLOCK_HEIGHT);
    public static HistoricalMoments AT_FIX_BLOCK_2 = new HistoricalMoments(67000, Props.AT_FIX_BLOCK_2_BLOCK_HEIGHT);
    public static HistoricalMoments AT_FIX_BLOCK_3 = new HistoricalMoments(92000, Props.AT_FIX_BLOCK_3_BLOCK_HEIGHT);
    public static HistoricalMoments AT_FIX_BLOCK_4 = new HistoricalMoments(255000, Props.AT_FIX_BLOCK_4_BLOCK_HEIGHT);
    public static HistoricalMoments PRE_POC2 = new HistoricalMoments(500000, Props.PRE_POC2_BLOCK_HEIGHT);
    public static HistoricalMoments POC2 = new HistoricalMoments(502000, Props.POC2_BLOCK_HEIGHT);
    public static HistoricalMoments SODIUM = new HistoricalMoments(765_000, Props.SODIUM_BLOCK_HEIGHT);
    public static HistoricalMoments SIGNUM = new HistoricalMoments(875_500, Props.SIGNUM_HEIGHT);
    public static HistoricalMoments POC_PLUS = new HistoricalMoments(878_000, Props.POC_PLUS_HEIGHT);
    public static HistoricalMoments SPEEDWAY = new HistoricalMoments(Integer.MAX_VALUE, Props.SPEEDWAY_HEIGHT);
    public static HistoricalMoments NEXT_FORK = new HistoricalMoments(Integer.MAX_VALUE, Props.DEV_NEXT_FORK_BLOCK_HEIGHT);

    private final int mainnetHeight;
    private final Prop<Integer> overridingProperty;

    HistoricalMoments(int mainnetHeight, Prop<Integer> overridingProperty) {
        this.mainnetHeight = mainnetHeight;
        this.overridingProperty = overridingProperty;
    }

    public int getMainnetHeight() {
        return mainnetHeight;
    }

    public Prop<Integer> getOverridingProperty() {
        return overridingProperty;
    }
}
