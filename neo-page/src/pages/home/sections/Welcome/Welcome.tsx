import { Suspense } from "react";
import { defaultContainer } from "@/styles/containers.css";
import { Spatial } from "./components/Spatial";
import * as classes from "./Welcome.css";

export const Welcome = () => {
  return (
    <section className={defaultContainer}>
      <div className={classes.contentContainer}>
        <div className={classes.contentSection}>
          <h1 className={classes.title}>
            A eco-friendly blockchain ♻️ platform fulfilling all your needs
          </h1>

          <p className={classes.description}>
            Welcome to Signum, where innovation meets simplicity, and your
            business transforms into a smarter, more efficient powerhouse.
          </p>
        </div>

        <div className={classes.spatialContainer}>
          <Suspense>
            <Spatial />
          </Suspense>
        </div>
      </div>
    </section>
  );
};
