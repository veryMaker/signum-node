import { Link } from "react-router-dom";
import { FaList, FaBookOpen } from "react-icons/fa6";
import { defaultContainer } from "@/styles/containers.css";
import * as classes from "./PathCta.css";

export const PathCta = () => {
  return (
    <section className={defaultContainer}>
      <div className={classes.container}>
        <img src="/images/signum-symbol.webp" alt="Signum Symbol" width={50} />

        <h6 className={classes.title}>
          Blockchain Reimagined. <br /> Available now.
        </h6>
      </div>

      <div className={classes.btnContainer}>
        <Link to="/apps" className={classes.button}>
          <FaList /> Explore Apps
        </Link>

        <Link to="/docs" className={classes.button}>
          <FaBookOpen /> Read the docs
        </Link>
      </div>
    </section>
  );
};
