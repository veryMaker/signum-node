import { defaultContainer } from "@/styles/containers.css";
import { ShortcutCard } from "@/components/ShortcutCard";
import * as classes from "./Shortcuts.css";

export const Shortcuts = () => {
  const tradeLinks = [
    {
      title: "Cashback",
      description:
        "Get 25% cashback On every transaction fee Created on your node",
      href: "https://docs.signum.network/signum/activate-cashback",
      cta: "Learn More",
    },
    {
      title: "Spot",
      description: "Trade Smart Tokens instantly on Signum Blockchain.",
      href: "https://docs.signum.network/signum/activate-cashback",
      cta: "Learn More",
    },
    {
      title: "Liquidity Pools",
      description: "Swap Signa with Smart tokens or earn trading fees.",
      href: "https://docs.signum.network/signum/activate-cashback",
      cta: "Learn More",
    },
    {
      title: "CEX",
      description: "Discover exchanges where Signum is available",
      href: "https://docs.signum.network/signum/activate-cashback",
      cta: "Learn More",
    },
  ];

  return (
    <section className={defaultContainer}>
      <h2 className={classes.title}>😎 Boss, what do you want to do?</h2>
      <p className={classes.description}>
        No worries if you don't know yet. Take your time to explore the
        ecosystem.
      </p>

      <ShortcutCard
        side="left"
        imgSrc="/images/woman-holding-hdd.webp"
        title="Trade"
        links={tradeLinks}
      />

      <ShortcutCard
        side="right"
        imgSrc="/images/woman-holding-hdd.webp"
        title="Trade"
        links={tradeLinks}
      />

      <ShortcutCard
        side="left"
        imgSrc="/images/woman-holding-hdd.webp"
        title="Trade"
        links={tradeLinks}
      />

      <ShortcutCard
        side="right"
        imgSrc="/images/woman-holding-hdd.webp"
        title="Trade"
        links={tradeLinks}
      />

      <ShortcutCard
        side="left"
        imgSrc="/images/woman-holding-hdd.webp"
        title="Trade"
        links={tradeLinks}
      />

      <ShortcutCard
        side="right"
        imgSrc="/images/woman-holding-hdd.webp"
        title="Trade"
        links={tradeLinks}
      />
    </section>
  );
};
