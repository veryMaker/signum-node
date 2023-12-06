import { Fragment } from "react";
import { MainApps } from "./sections/MainApps";
import { Cashback } from "./sections/Cashback";
import { Stats } from "./sections/Stats";
import { Wikis } from "./sections/Wikis";

export function HomePage() {
  return (
    <Fragment>
      <MainApps />
      <Cashback />
      <Stats />
      <Wikis />
    </Fragment>
  );
}
