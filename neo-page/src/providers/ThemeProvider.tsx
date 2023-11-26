import { type ReactElement, Fragment } from "react";
import { type CSSObject, Global } from "@emotion/react";
import {
  createAppStylesBaseline,
  AnimatorGeneralProvider,
  type BleepsProviderSettings,
  BleepsProvider,
} from "@arwes/react";
import { Background } from "@/components/Background";
import { theme } from "@/types";

interface Props {
  children: ReactElement;
}

const stylesBaseline = createAppStylesBaseline(theme);

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
          <Global styles={stylesBaseline as Record<string, CSSObject>} />
          <main style={{ position: "relative" }}>
            <Background theme={theme} />
            <div style={{ position: "relative" }}>{children}</div>
          </main>
        </BleepsProvider>
      </AnimatorGeneralProvider>
    </Fragment>
  );
}
