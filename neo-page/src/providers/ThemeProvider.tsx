import { type ReactElement, Fragment } from "react";
import {
  AnimatorGeneralProvider,
  type BleepsProviderSettings,
  BleepsProvider,
} from "@arwes/react";
import { Background } from "@/components/Background";
import { theme } from "@/types";
import "@/styles/global.css";

interface Props {
  children: ReactElement;
}

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

export function ThemeProvider({ children }: Props) {
  return (
    <Fragment>
      <AnimatorGeneralProvider>
        <BleepsProvider {...bleepsSettings}>
          <main style={{ position: "relative" }}>
            <Background theme={theme} />
            <div style={{ position: "relative" }}>{children}</div>
          </main>
        </BleepsProvider>
      </AnimatorGeneralProvider>
    </Fragment>
  );
}
