import { FaGithub, FaYoutube } from "react-icons/fa";
import { FaXTwitter, FaDiscord } from "react-icons/fa6";
import { useSound } from "@/hooks/useSound";
import { githubLink, twitterLink, youtubeLink, discordLink } from "@/types";

import * as classes from "./SocialLinks.css";

export const SocialLinks = () => {
  const { playClickSound } = useSound();

  return (
    <div className={classes.contentContainer}>
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
