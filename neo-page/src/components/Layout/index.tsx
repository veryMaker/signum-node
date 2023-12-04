import { Fragment } from "react";
import { Outlet } from "react-router-dom";
import { theme } from "@/types";
import { Providers } from "@/providers";
import { Background } from "./components/Background";
import { Header } from "./components/Header";
import { Alerts } from "./components/Alerts";

export const Layout = () => {
  return (
    <Providers>
      <main style={{ position: "relative", overflow: "hidden" }}>
        <Background theme={theme} />
        <div style={{ position: "relative" }}>
          {
            <Fragment>
              <Alerts />
              <Header />
              <Outlet />
            </Fragment>
          }
        </div>
      </main>
    </Providers>
  );
};
