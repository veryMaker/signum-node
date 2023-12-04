import { FaGithub, FaYoutube } from "react-icons/fa";
import { FaXTwitter, FaDiscord } from "react-icons/fa6";
import { useSound } from "@/hooks/useSound";
import * as classes from "./Header.css";

export const Header = () => {
  const { playClickSound } = useSound();

  const githubLink = "https://github.com/signum-network/signum-node";
  const twitterLink = "https://twitter.com/signum_official";
  const youtubeLink = "https://www.youtube.com/@SignumNetwork";
  const discordLink = "https://discord.com/invite/FATkyfNMxU";

  return (
    <header className={classes.headerContainer}>
      <img
        src="/images/signum-neon-logo.webp"
        width={150}
        title="Signum Neon Logo"
      />

      <div className={classes.contentContainer}>
        <span className={classes.versionTag}>v0.0.0</span>

        <a
          href={githubLink}
          target="_blank"
          className={classes.avatar}
          onClick={playClickSound}
        >
          <FaGithub />
        </a>

        <a
          href={twitterLink}
          target="_blank"
          className={classes.avatar}
          onClick={playClickSound}
        >
          <FaXTwitter />
        </a>

        <a
          href={youtubeLink}
          target="_blank"
          className={classes.avatar}
          onClick={playClickSound}
        >
          <FaYoutube />
        </a>

        <a
          href={discordLink}
          target="_blank"
          className={classes.avatar}
          onClick={playClickSound}
        >
          <FaDiscord />
        </a>
      </div>
    </header>
  );
};
