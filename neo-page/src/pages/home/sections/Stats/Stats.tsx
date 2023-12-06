import { NeoChip } from "@/components/NeoChip";
import { useNodeState } from "@/hooks/useNodeState";
import {
  defaultContainer,
  defaultCardContainer,
} from "@/styles/containers.css";
import { KpiCard } from "./components/KpiCard";
import * as classes from "./Stats.css";

export const Stats = () => {
  const {
    burnedFunds,
    circulatingFunds,
    numberOfATs,
    numberOfAliases,
    numberOfAssets,
    numberOfSubscriptionPayments,
    numberOfSubscriptions,
    numberOfTransactions,
    isLoading,
  } = useNodeState();

  return (
    <section className={defaultContainer}>
      <div className={classes.titleContainer}>
        <NeoChip label="Stats" />
        <span className={classes.genesisText}>Since Genesis Block. 2014</span>
      </div>

      <div className={defaultCardContainer}>
        <KpiCard
          title="Total Transactions"
          value={numberOfTransactions}
          isLoading={isLoading}
        />
        <KpiCard
          title="Smart Contracts"
          value={numberOfATs}
          isLoading={isLoading}
        />
        <KpiCard
          title="Tokens Created"
          value={numberOfAssets}
          isLoading={isLoading}
        />
        <KpiCard
          title="Aliases Minted"
          value={numberOfAliases}
          isLoading={isLoading}
        />
        <KpiCard
          title="Subscriptions Created"
          value={numberOfSubscriptions}
          isLoading={isLoading}
        />
        <KpiCard
          title="Subscription Payments"
          value={numberOfSubscriptionPayments}
          isLoading={isLoading}
        />
        <KpiCard
          title="SIGNA in circulation"
          value={circulatingFunds}
          isLoading={isLoading}
        />
        <KpiCard
          title="SIGNA Burned 🔥"
          value={burnedFunds}
          isBurning
          isLoading={isLoading}
        />
      </div>
    </section>
  );
};
