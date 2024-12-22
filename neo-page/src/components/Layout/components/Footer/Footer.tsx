import { githubLink } from "@/types";

export const Footer = () => (
  <footer
    style={{
      position: "relative",
      display: "flex",
      justifyContent: "center",
      paddingBottom: "1rem",
    }}
  >
    <a href={githubLink} target="_blank">
      Github Repo
    </a>
    - Made with ❤️ By Signum Network
  </footer>
);
