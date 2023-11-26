import { css } from "@emotion/react";
import { FaGithub, FaYoutube } from "react-icons/fa";
import { Illuminator } from "@arwes/react-frames";
import { FaXTwitter, FaDiscord } from "react-icons/fa6";
import { defaultMaxWidth } from "@/types";
import { theme } from "@/types";
import { useSound } from "@/hooks/useSound";

export const Header = () => {
  const { playClickSound } = useSound();

  const githubLink = "https://github.com/signum-network/signum-node";
  const twitterLink = "https://twitter.com/signum_official";
  const youtubeLink = "https://www.youtube.com/@SignumNetwork";
  const discordLink = "https://discord.com/invite/FATkyfNMxU";

  const defaultIconSize = 26;
  const defaultColor = theme.colors.primary.deco(100);
  const defaultStyles = {
    fontSize: defaultIconSize,
    color: defaultColor,
    outline: "none",
  };

  return (
    <header
      style={{
        position: "sticky",
        overflow: "hidden",
      }}
    >
      <div
        css={css`
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 1rem;
          width: 100%;
          max-width: ${defaultMaxWidth}px;
          margin: auto;
          flex-wrap: wrap;

          @media (max-width: 600px) {
            justify-content: center;
            gap: 1rem;
          }
        `}
      >
        <img src="/images/signum-neon-logo.webp" width={150} />

        <div
          style={{ display: "flex", alignItems: "center", gap: 16, zIndex: 1 }}
        >
          <span
            style={{
              color: theme.colors.primary.deco(100),
              background: theme.colors.primary.deco(5),
              fontWeight: "bold",
              padding: "0.5rem 1rem",
              borderRadius: 4,
            }}
          >
            v0.0.0
          </span>

          <a
            href={githubLink}
            target="_blank"
            style={defaultStyles}
            onClick={playClickSound}
          >
            <FaGithub />
          </a>

          <a
            href={twitterLink}
            target="_blank"
            style={defaultStyles}
            onClick={playClickSound}
          >
            <FaXTwitter />
          </a>

          <a
            href={youtubeLink}
            target="_blank"
            style={defaultStyles}
            onClick={playClickSound}
          >
            <FaYoutube />
          </a>

          <a
            href={discordLink}
            target="_blank"
            style={defaultStyles}
            onClick={playClickSound}
          >
            <FaDiscord />
          </a>
        </div>

        <svg
          style={{
            width: "100%",
            height: "100%",
            position: "absolute",
          }}
          viewBox="0 0 1000 82"
        ></svg>
      </div>

      <Illuminator
        color="hsl(180 50% 50% / 20%)"
        size={82}
        style={{ height: "100%", position: "absolute", top: 0 }}
      />
    </header>
  );
};
