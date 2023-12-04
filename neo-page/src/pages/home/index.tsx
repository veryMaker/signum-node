import { Fragment } from "react";
import { MainApps } from "./sections/MainApps";
import { Stats } from "./sections/Stats";
import { Wikis } from "./sections/Wikis";

export function HomePage() {
  return (
    <Fragment>
      <MainApps />
      <Stats />
      <Wikis />
    </Fragment>
  );
}
