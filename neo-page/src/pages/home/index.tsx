import { Fragment } from "react";
import { Cashback } from "./sections/Cashback";
import { Stats } from "./sections/Stats";

export function HomePage() {
  return (
    <Fragment>
      <Cashback />
      <Stats />
    </Fragment>
  );
}
