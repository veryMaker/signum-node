import { style } from "@vanilla-extract/css";

export const defaultBG = style({
  position: "fixed",
  left: 0,
  right: 0,
  top: 0,
  bottom: 0,
  background: `linear-gradient(rgba(0%,0%,0%,80%), rgba(0%,0%,0%,80%) ), url("/images/bg.webp")`,
  backgroundSize: "cover",
  backgroundPosition: "center",
});

export const illuminatorContainer = style({
  width: "100%",
  height: "100%",
  position: "relative",
});

export const svgEffect = style({
  width: "100%",
  height: "100%",
  position: "absolute",
});

export const illuminator = style({
  height: "100%",
  position: "absolute",
  top: 0,
});
