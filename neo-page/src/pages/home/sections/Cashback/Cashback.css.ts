import { style } from "@vanilla-extract/css";
import { theme } from "@/styles/theme";

export const banner = style({
  display: "flex",
  flexDirection: "column",
  justifyContent: "center",
  alignItems: "center",
  position: "relative",
  zIndex: 0,
  width: "100%",
  padding: "1rem",
  minHeight: 100,
  fontSize: "2rem",
  fontWeight: 700,
  color: "white",
  gap: 8,
});

export const button = style({
  margin: 0,
  padding: "0 12px",
  color: theme.colors.secondary.deco(100),
  background: theme.colors.secondary.deco(5),
  fontSize: "1.3rem",
  zIndex: 0,
  position: "relative",
});

export const buttonFrame = style({
  // @ts-expect-error special arwes css
  "[data-name=bg]": { color: "hsla(180, 75%, 10%, 75%)" },
  "[data-name=line]": { color: theme.colors.secondary.deco(5) },
});

export const frame = style({
  // @ts-expect-error special arwes css
  "[data-name=bg]": {
    color: "hsla(180, 75%, 10%, 80%)",
  },
  "[data-name=line]": { color: "hsl(180, 75%, 75%)" },
  background: "url(/images/galaxy.webp)",
  backgroundSize: "cover",
  backgroundPosition: "center",
  overflow: "hidden",
});
