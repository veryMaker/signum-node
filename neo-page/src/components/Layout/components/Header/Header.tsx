import { NavLinks } from "./components/NavLinks";
import { SocialLinks } from "./components/SocialLinks";
import * as classes from "./Header.css";

export const Header = () => {
  return (
    <header className={classes.headerContainer}>
      <img
        src="/images/signum-neon-logo.webp"
        width={150}
        title="Signum Neon Logo"
      />

      <section className={classes.navLinksContainer}>
        <NavLinks />
      </section>

      <section className={classes.socialLinksContainer}>
        <SocialLinks />
      </section>
    </header>
  );
};
