import { Fragment } from "react";
import { useNodeConstants } from "@/hooks/useNodeConstants";
import { NeoChip } from "@/components/NeoChip";
import { AppCard } from "./components/AppCard";
import { defaultContainer } from "@/styles/containers.css";
import * as classes from "./MainApps.css";

export const MainApps = () => {
  const { cashBackRS, isDefaultCashbackIdSet } = useNodeConstants();

  return (
    <Fragment>
      <section className={defaultContainer}>
        <div className={classes.titleContainer}>
          <NeoChip label="Discover" />
        </div>

        <div className={classes.cardsContainer}>
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
            description="Powerful alias platform"
            background="linear-gradient(227deg, #D481FF 0%, #8855F9 102.12%)"
            img="/images/alias.webp"
            url="./sns/index.html"
          />

          <AppCard
            title="Neoclassic Wallet"
            description="Modernized version of Classic Wallet"
            background="linear-gradient(227deg, #F06082 0%, #E94F74 102.12%)"
            img="/images/neo-wallet.webp"
            url="./neo-classic/index.html"
          />

          <AppCard
            title="Mobile Wallet"
            description="Manage your digital assets on-the-go!"
            background="#618AFF"
            img="/images/mobile.webp"
            url="https://docs.signum.network/ecosystem/wallets#f3Qr1"
          />

          <AppCard
            title="XT Wallet"
            description="Wallet as a web extension for your browser"
            background="#90875C"
            img="/images/extension.webp"
            url="https://docs.signum.network/ecosystem/wallets#jjLLe"
          />
        </div>
      </section>

      <section className={defaultContainer}>
        <div className={classes.titleContainer}>
          <div>
            <NeoChip label="Features" />
          </div>
        </div>

        <div className={classes.cardsContainer}>
          <AppCard
            title="API Docs"
            secondTitle="For developers"
            description="Documentation of Self Hosted API"
            background="linear-gradient(134deg, #FFF 0%, #D7E3EF 100%)"
            img="/images/api-icon.webp"
            url="./api-doc/index.html"
          />

          {!isDefaultCashbackIdSet && (
            <AppCard
              title="Cashback"
              secondTitle="✅ Succesfully set"
              description={`Account: ${cashBackRS}`}
              background="linear-gradient(180deg, #35D586 0%, #1E8C5E 100%)"
              img="/images/cashback.webp"
              url="https://docs.signum.network/signum/activate-cashback"
            />
          )}
        </div>
      </section>
    </Fragment>
  );
};
