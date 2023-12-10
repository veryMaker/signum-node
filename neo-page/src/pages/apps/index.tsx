import { Fragment } from "react";
import { MainApps } from "./sections/MainApps";
import { ThirdPartyApps } from "./sections/ThirdPartyApps";

export function AppsPage() {
  return (
    <Fragment>
      <MainApps />
      <ThirdPartyApps />
    </Fragment>
  );
}
