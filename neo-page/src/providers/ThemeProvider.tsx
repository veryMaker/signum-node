import { Fragment } from "react";
import {
  AnimatorGeneralProvider,
  type BleepsProviderSettings,
  BleepsProvider,
} from "@arwes/react";
import "@/styles/global.css";
import type { Props } from "./types";

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

// Providers related to arwes framework goes here

export function ThemeProvider({ children }: Props) {
  return (
    <Fragment>
      <AnimatorGeneralProvider>
        <BleepsProvider {...bleepsSettings}>{children}</BleepsProvider>
      </AnimatorGeneralProvider>
    </Fragment>
  );
}
