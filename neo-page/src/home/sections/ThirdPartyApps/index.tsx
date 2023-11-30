import { NeoChipOld } from "@/components/NeoChipOld.tsx";
import { AppCard } from "./components/AppCard";
import { ThirdPartyApp } from "./types";

export const ThirdPartyApps = () => {
  return (
    <section className="default-container">
      <div style={{ display: "flex" }}>
        <NeoChipOld label="Third Party Apps" />
      </div>

      <div
        className="default-cards-container"
        style={{ justifyContent: "flex-start" }}
      >
        <AppCard
          title="Fomplo Crypto Calculator"
          description="Provide liquidity on the Signum  Network, an open protocol to trade Signum Network for more more and more because you deserve it"
          href="https://www.fomplo.com/calculator"
          imgSrc="https://www.fomplo.com/assets/home/crypto.webp"
        />
      </div>
    </section>
  );
};
