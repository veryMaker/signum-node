import {
  defaultContainer,
  defaultCardContainer,
} from "@/styles/containers.css";
import { NeoChip } from "@/components/NeoChip";
import { AppCard } from "./components/AppCard";

export const ThirdPartyApps = () => {
  return (
    <section className={defaultContainer}>
      <div style={{ display: "flex" }}>
        <NeoChip label="Third Party Apps" />
      </div>

      <div
        className={defaultCardContainer}
        style={{ justifyContent: "flex-start" }}
      >
        <AppCard
          title="Fomplo Crypto Calculator"
          description="Provide liquidity on the Signum  Network, an open protocol to trade Signum Network for more more and more because you deserve it"
          url="https://www.fomplo.com/calculator"
          img="https://www.fomplo.com/assets/home/crypto.webp"
        />
        <AppCard
          title="Fomplo Crypto Calculator"
          description="Provide liquidity on the Signum  Network, an open protocol to trade Signum Network for more more and more because you deserve it"
          url="https://www.fomplo.com/calculator"
          img="https://www.fomplo.com/assets/home/crypto.webp"
        />
        <AppCard
          title="Fomplo Crypto Calculator"
          description="Provide liquidity on the Signum  Network, an open protocol to trade Signum Network for more more and more because you deserve it"
          url="https://www.fomplo.com/calculator"
          img="https://www.fomplo.com/assets/home/crypto.webp"
        />
        <AppCard
          title="Fomplo Crypto Calculator"
          description="Provide liquidity on the Signum  Network, an open protocol to trade Signum Network for more more and more because you deserve it"
          url="https://www.fomplo.com/calculator"
          img="https://www.fomplo.com/assets/home/crypto.webp"
        />
        <AppCard
          title="Fomplo Crypto Calculator"
          description="Provide liquidity on the Signum  Network, an open protocol to trade Signum Network for more more and more because you deserve it"
          url="https://www.fomplo.com/calculator"
          img="https://www.fomplo.com/assets/home/crypto.webp"
        />
      </div>
    </section>
  );
};
