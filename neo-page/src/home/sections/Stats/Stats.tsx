import { NeoChip } from "@/components/NeoChip";
import {
  defaultContainer,
  defaultCardContainer,
} from "@/styles/containers.css";
import { KpiCard } from "./components/KpiCard";
import * as classes from "./Stats.css";

export const Stats = () => {
  return (
    <section className={defaultContainer}>
      <div className={classes.titleContainer}>
        <NeoChip label="Stats" />
        <span className={classes.genesisText}>Since Genesis Block. 2014</span>
      </div>

      <div className={defaultCardContainer}>
        <KpiCard title="Total Transactions" value={10298072} />
        <KpiCard title="Smart Contracts" value={69517} />
        <KpiCard title="Tokens Created" value={680} />
        <KpiCard title="Aliases Minted" value={62565} />
        <KpiCard title="Subscriptions Created" value={257} />
        <KpiCard title="Subscription Payments" value={123887} />
        <KpiCard title="SIGNA in circulation" value={2161378457} />
        <KpiCard title="Total of SIGNA Burned 🔥" value={1016442} />
      </div>
    </section>
  );
};
