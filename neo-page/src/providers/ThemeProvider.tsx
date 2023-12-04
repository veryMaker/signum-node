import { Outlet } from "react-router-dom";
import { Fragment } from "react";
import {
  AnimatorGeneralProvider,
  type BleepsProviderSettings,
  BleepsProvider,
} from "@arwes/react";
import { Background } from "@/components/Background";
import { theme } from "@/types";
import "@/styles/global.css";

const bleepsSettings: BleepsProviderSettings = {
  master: { volume: 0.75 },
  bleeps: {
    // A transition bleep sound to play when the user enters the app.
    intro: {
      sources: [{ src: "/sounds/intro.mp3", type: "audio/mpeg" }],
    },
    // An interactive bleep sound to play when user clicks.
    click: {
      sources: [{ src: "/sounds/click.mp3", type: "audio/mpeg" }],
    },
  },
};

export function ThemeProvider() {
  return (
    <Fragment>
      <AnimatorGeneralProvider>
        <BleepsProvider {...bleepsSettings}>
          <main style={{ position: "relative", overflow: "hidden" }}>
            <Background theme={theme} />
            <div style={{ position: "relative" }}>{<Outlet />}</div>
          </main>
        </BleepsProvider>
      </AnimatorGeneralProvider>
    </Fragment>
  );
}
