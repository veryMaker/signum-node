import { defaultContainer } from "@/styles/containers.css";
import { NeoChip } from "@/components/NeoChip";
import { ShortcutCard } from "@/components/ShortcutCard";

export const Developer = () => {
  const developerLinks = [
    {
      title: "Smart Contracts",
      description:
        "Get 25% cashback On every transaction fee Created on your node",
      href: "https://docs.signum.network/signum/activate-cashback",
      cta: "Learn More",
    },
    {
      title: "API Docs",
      description: "Trade Smart Tokens instantly on Signum Blockchain.",
      href: "https://docs.signum.network/signum/activate-cashback",
      cta: "Learn More",
    },
    {
      title: "SignumJS",
      description: "Swap Signa with Smart tokens or earn trading fees.",
      href: "https://docs.signum.network/signum/activate-cashback",
      cta: "Learn More",
    },
    {
      title: "SmartC",
      description: "Discover exchanges where Signum is available",
      href: "https://docs.signum.network/signum/activate-cashback",
      cta: "Learn More",
    },
  ];

  return (
    <section className={defaultContainer}>
      <div style={{ display: "flex", marginBottom: "2rem" }}>
        <NeoChip label="Young padawan" />
      </div>

      <ShortcutCard
        side="left"
        imgSrc="/images/developers-ballmer-min.gif"
        title="Developers, Developers, Developers"
        links={developerLinks}
      />
    </section>
  );
};
