import { Link } from "react-router-dom";
import { FaDiscord } from "react-icons/fa6";
import { defaultContainer } from "@/styles/containers.css";
import { discordLink } from "@/types";
import * as classes from "./CommunityCta.css";

export const CommunityCta = () => {
  return (
    <section className={defaultContainer}>
      <div className={classes.container}>
        <img src="/images/signum-symbol.webp" alt="Signum Symbol" width={50} />

        <h6 className={classes.title}>
          Do not know how to start? <br /> Do not worry, Join the community!
        </h6>
      </div>

      <div className={classes.btnContainer}>
        <Link to={discordLink} target="_blank" className={classes.button}>
          <FaDiscord /> Join the community
        </Link>
      </div>
    </section>
  );
};
