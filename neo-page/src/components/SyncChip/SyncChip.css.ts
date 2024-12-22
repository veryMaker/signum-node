import { style } from "@vanilla-extract/css";

export const container = style({
  position: "relative",
  zIndex: 11,
  padding: "1rem 2rem",
  overflow: "hidden",
});

export const chip = style({
  color: "hsl(152, 39%, 50%)",
  fontWeight: 900,
  margin: 0,
  textShadow: "0 0 15px hsl(152, 39%, 50%) ",
  zIndex: 100,
  width: "100%",
  textAlign: "center",
  display: "inline-block",
});

export const frame = style({
  // @ts-expect-error special arwes css
  "[data-name=bg]": { color: "hsl(152, 39%, 10%)" },
  "[data-name=line]": { color: "hsl(152, 39%, 50%)" },
});

export const puff = style({
  position: "absolute",
  width: "100%",
  height: "100%",
  top: 0,
  left: 0,
});
