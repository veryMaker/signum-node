import { Fragment } from "react";
import { Header } from "@/components/Header";
import { MainApps } from "./sections/MainApps";

export function HomePage() {
  return (
    <Fragment>
      <Header />

      <MainApps />
    </Fragment>
  );
}
