import { Fragment } from "react";
import { Header } from "@/components/Header";
import { MainApps } from "./sections/MainApps";
import { Stats } from "./sections/Stats";
import { Wikis } from "./sections/Wikis";
import { ThirdPartyApps } from "./sections/ThirdPartyApps";

export function HomePage() {
  return (
    <Fragment>
      <Header />

      <MainApps />
      <Stats />
      <Wikis />
      <ThirdPartyApps />
    </Fragment>
  );
}
