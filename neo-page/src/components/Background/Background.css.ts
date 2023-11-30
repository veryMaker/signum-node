import { style } from "@vanilla-extract/css";

export const defaultBG = style({
  position: "absolute",
  left: 0,
  right: 0,
  top: 0,
  bottom: 0,
  backgroundImage:
    "radial-gradient(85% 85% at 50% 50%, hsla(185, 100%, 25%, 0.25) 0%, hsla(185, 100%, 25%, 0.12) 50%, hsla(185, 100%, 25%, 0) 100%)",
});
