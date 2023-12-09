import { Link, useLocation } from "react-router-dom";
import { FaHouse, FaList } from "react-icons/fa6";
import { useSound } from "@/hooks/useSound";
import { FrameSVGCorners } from "@arwes/react-frames";
import * as classes from "./NavLinks.css";

export const NavLinks = () => {
  const { playClickSound } = useSound();
  const { pathname } = useLocation();

  const isUserOnHomePage = pathname === "/";
  const isUserOnThirdPartyAppsPage = pathname === "/third-party";

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
        to="/third-party"
        onClick={playClickSound}
        style={{ outline: "none" }}
        className={classes.link({
          variant: isUserOnThirdPartyAppsPage ? "active" : "inactive",
        })}
      >
        <FaList /> Explore Apps
        {isUserOnThirdPartyAppsPage && (
          <FrameSVGCorners className={classes.frame} />
        )}
      </Link>
    </div>
  );
};
