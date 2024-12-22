import { Fragment } from "react";
import { Welcome } from "./sections/Welcome";
import { HeroBanner } from "./sections/HeroBanner";
import { Shortcuts } from "./sections/Shortcuts";
import { PathCta } from "./sections/PathCta";

export function HomePage() {
  return (
    <Fragment>
      <Welcome />
      <HeroBanner />
      <Shortcuts />
      <PathCta />
    </Fragment>
  );
}
