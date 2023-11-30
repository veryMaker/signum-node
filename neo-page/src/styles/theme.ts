import { type AppTheme as Theme, createAppTheme } from "@arwes/react";

const defaultFont = `"Titillium Web","Segoe UI Web (West European)","Segoe UI",-apple-system,BlinkMacSystemFont,Roboto,"Helvetica Neue",sans-serif`;

const theme = createAppTheme({
  settings: {
    hues: {
      primary: 180,
      secondary: 60,
    },
    fontFamilies: {
      title: defaultFont,
      body: defaultFont,
      cta: defaultFont,
      input: defaultFont,
      code: "JetBrains Mono,Menlo,Monaco,Consolas,Courier New,monospace",
    },
  },
});

export type { Theme };
export { theme };
