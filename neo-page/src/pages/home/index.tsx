import { Fragment } from "react";
import { Welcome } from "./sections/Welcome";
import { HeroBanner } from "./sections/HeroBanner";
import { Stats } from "./sections/Stats";
import { Shortcuts } from "./sections/Shortcuts";
import { PathCta } from "./sections/PathCta";

export function HomePage() {
  return (
    <Fragment>
      <Welcome />
      <HeroBanner />
      <Shortcuts />
      <Stats />
      <PathCta />
    </Fragment>
  );
}
