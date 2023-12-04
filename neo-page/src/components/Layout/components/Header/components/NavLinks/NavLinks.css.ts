import { style } from "@vanilla-extract/css";
import { recipe } from "@vanilla-extract/recipes";
import { theme } from "@/styles/theme";

export const contentContainer = style({
  display: "flex",
  alignItems: "center",
  gap: 16,
  zIndex: 1,
});

export const link = recipe({
  base: {
    display: "flex",
    alignItems: "center",
    gap: 4,
    fontSize: "1.1rem",
    outline: "none",
    position: "relative",
    padding: "0.5rem 0.9rem",
  },
  variants: {
    variant: {
      active: { color: theme.colors.primary.deco(25) },
      inactive: { color: "rgba(255,255,255,0.5)" },
    },
  },
});

export const frame = style({
  // @ts-expect-error special arwes css
  "[data-name=bg]": { color: "hsl(180, 75%, 10%)" },
  "[data-name=line]": { color: "hsl(180, 75%, 50%)" },
});
