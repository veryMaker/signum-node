import { Fragment } from "react";
import { Header } from "@/components/Header";
import { MainApps } from "./sections/MainApps";
import { Stats } from "./sections/Stats";
import { Wikis } from "./sections/Wikis";

export function HomePage() {
  return (
    <Fragment>
      <Header />

      <MainApps />
      <Stats />
      <Wikis />
    </Fragment>
  );
}
