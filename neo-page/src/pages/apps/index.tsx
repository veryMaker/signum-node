import { Fragment, useEffect } from "react";
import { MainApps } from "./sections/MainApps";
import { ThirdPartyApps } from "./sections/ThirdPartyApps";

export function AppsPage() {
  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  return (
    <Fragment>
      <MainApps />
      <ThirdPartyApps />
    </Fragment>
  );
}
