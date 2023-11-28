import { css } from "@emotion/react";
import { NeoChip } from "@/components/NeoChip";
import { SyncChip } from "@/components/SyncChip";
import { AppCard } from "./components/AppCard";

export const MainApps = () => {
  return (
    <section className="default-container">
      <div
        css={css`
          display: flex;
          justify-content: space-between;
          align-items: center;

          @media (max-width: 600px) {
            flex-direction: column-reverse;
            justify-content: center;
            align-items: stretch;
            gap: 1rem;
          }
        `}
      >
        <NeoChip label="Discover" />
        <SyncChip />
      </div>

      <div
        style={{
          display: "flex",
          justifyContent: "flex-start",
          alignItems: "center",
          flexWrap: "wrap",
          gap: "4rem 1rem",
          marginTop: "3rem",
        }}
      >
        <AppCard
          title="Phoenix Wallet"
          description="Zero config wallet"
          background="linear-gradient(226deg, #0071D9 0%, #004F99 100%)"
          img="/images/phoenix.webp"
          url="./phoenix/index.html"
        />

        <AppCard
          title="SignumSwap"
          description="Easy DeFi Portal at your fingertips"
          background="linear-gradient(227deg, #FAB83E 0%, #FF8006 102.12%)"
          img="/images/signum-translucid.webp"
          url="./defi/index.html"
        />

        <AppCard
          title="Signum Name System"
          description="Self hosted Powerful Alias Platform"
          background="linear-gradient(227deg, #D481FF 0%, #8855F9 102.12%)"
          img="/images/signum-translucid.webp"
          url="./sns/index.html"
        />

        <AppCard
          title="Neoclassic Wallet"
          description="Modernized version of Classic Wallet"
          background="linear-gradient(227deg, #F06082 0%, #E94F74 102.12%)"
          initial="NC"
          url="./neo-classic/index.html"
        />

        <AppCard
          title="Classic Wallet"
          description="Initial version of Signum wallet"
          background="#1F2124"
          initial="C"
          url="./classic/index.html"
        />

        <AppCard
          title="API Docs"
          description="Documentation of Self Hosted API"
          background="linear-gradient(134deg, #FFF 0%, #D7E3EF 100%)"
          img="/images/api-icon.webp"
          url="./api-doc/index.html"
        />
      </div>
    </section>
  );
};
