import { NeoChip } from "@/components/NeoChip";
import {
  defaultContainer,
  defaultCardContainer,
} from "@/styles/containers.css";
import { WikiCard } from "./components/WikiCard";

export const Wikis = () => {
  return (
    <section className={defaultContainer}>
      <div style={{ display: "flex" }}>
        <NeoChip label="Wikis" />
      </div>

      <div className={defaultCardContainer}>
        <WikiCard
          title="Signum Docs"
          description="Basic Signum Wiki"
          href="https://docs.signum.network/signum"
        />

        <WikiCard
          title="Ecosystem"
          description="Discover the ecosystem"
          href="https://docs.signum.network/ecosystem"
        />

        <WikiCard
          title="Wiki"
          description="Community-driven docs"
          href="https://wiki.signum.network"
        />

        <WikiCard
          title="Whitepaper"
          description="Business whitepaper"
          href="https://signum-network.github.io/whitepaper/business/v1.pdf"
        />
      </div>
    </section>
  );
};
