import { FaGithub, FaYoutube } from "react-icons/fa";
import { FaXTwitter, FaDiscord } from "react-icons/fa6";
import { useBlockchainStatus } from "@/hooks/useBlockchainStatus";
import { useSound } from "@/hooks/useSound";

import * as classes from "./SocialLinks.css";

export const SocialLinks = () => {
  const { playClickSound } = useSound();
  const { version, isLoading } = useBlockchainStatus();

  const githubLink = "https://github.com/signum-network/signum-node";
  const twitterLink = "https://twitter.com/signum_official";
  const youtubeLink = "https://www.youtube.com/@SignumNetwork";
  const discordLink = "https://discord.com/invite/FATkyfNMxU";

  return (
    <div className={classes.contentContainer}>
      {!isLoading && <span className={classes.versionTag}>{version}</span>}

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
  );
};
