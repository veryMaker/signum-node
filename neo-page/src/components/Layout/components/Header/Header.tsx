import { useBlockchainStatus } from "@/hooks/useBlockchainStatus";
import { NavLinks } from "./components/NavLinks";
import { SocialLinks } from "./components/SocialLinks";
import * as classes from "./Header.css";

export const Header = () => {
  const { version, isLoading } = useBlockchainStatus();

  return (
    <header className={classes.headerContainer}>
      <section className={classes.nodeVersionContainer}>
        <img
          src="/images/signum-neon-logo.webp"
          width={150}
          title="Signum Neon Logo"
        />

        {!isLoading && <span className={classes.versionTag}>{version}</span>}
      </section>

      <section className={classes.navLinksContainer}>
        <NavLinks />
      </section>

      <section className={classes.socialLinksContainer}>
        <SocialLinks />
      </section>
    </header>
  );
};
