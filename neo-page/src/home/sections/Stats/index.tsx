import { NeoChipOld } from "@/components/NeoChipOld.tsx";
import { theme } from "@/utils/theme";
import { KpiCard } from "./components/KpiCard";

export const Stats = () => {
  return (
    <section className="default-container">
      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: "1rem",
        }}
      >
        <NeoChipOld label="Stats" />
        <span style={{ color: theme.colors.warning.deco(30) }}>
          Since Genesis Block. 2014
        </span>
      </div>

      <div className="default-cards-container">
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
