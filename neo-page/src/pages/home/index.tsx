import { Fragment } from "react";
import { Cashback } from "./sections/Cashback";
import { Welcome } from "./sections/Welcome";
import { Stats } from "./sections/Stats";
import { Shortcuts } from "./sections/Shortcuts";

export function HomePage() {
  return (
    <Fragment>
      <Welcome />
      <Cashback />
      <Shortcuts />
      <Stats />
    </Fragment>
  );
}
