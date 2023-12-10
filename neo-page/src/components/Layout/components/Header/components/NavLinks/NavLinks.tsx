import { Link, useLocation } from "react-router-dom";
import { FaHouse, FaList, FaBookOpen } from "react-icons/fa6";
import { useSound } from "@/hooks/useSound";
import { FrameSVGCorners } from "@arwes/react-frames";
import * as classes from "./NavLinks.css";

export const NavLinks = () => {
  const { playClickSound } = useSound();
  const { pathname } = useLocation();

  const isUserOnHomePage = pathname === "/";
  const isUserOnAppsPage = pathname === "/apps";
  const isUserOnDocsPage = pathname === "/docs";

  return (
    <div className={classes.contentContainer}>
      <Link
        to="/"
        onClick={playClickSound}
        style={{ outline: "none" }}
        className={classes.link({
          variant: isUserOnHomePage ? "active" : "inactive",
        })}
      >
        <FaHouse /> Home
        {isUserOnHomePage && <FrameSVGCorners className={classes.frame} />}
      </Link>

      <Link
        to="/apps"
        onClick={playClickSound}
        style={{ outline: "none" }}
        className={classes.link({
          variant: isUserOnAppsPage ? "active" : "inactive",
        })}
      >
        <FaList /> Apps
        {isUserOnAppsPage && <FrameSVGCorners className={classes.frame} />}
      </Link>

      <Link
        to="/docs"
        onClick={playClickSound}
        style={{ outline: "none" }}
        className={classes.link({
          variant: isUserOnDocsPage ? "active" : "inactive",
        })}
      >
        <FaBookOpen /> Docs
        {isUserOnDocsPage && <FrameSVGCorners className={classes.frame} />}
      </Link>
    </div>
  );
};
