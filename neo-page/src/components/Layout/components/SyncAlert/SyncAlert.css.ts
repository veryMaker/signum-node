import { style } from "@vanilla-extract/css";

export const container = style({
  position: "fixed",
  bottom: "3%",
  right: "3%",
  padding: "1rem",
  zIndex: 11,
});

export const frame = style({
  // @ts-expect-error special arwes css
  "[data-name=bg]": {
    color: "hsla(180, 75%, 10%, 80%)",
  },
  "[data-name=line]": { color: "hsl(180, 75%, 75%)" },
  backgroundSize: "cover",
  backgroundPosition: "center",
  overflow: "hidden",
});
