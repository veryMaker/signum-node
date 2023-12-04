import { style } from "@vanilla-extract/css";
import { recipe } from "@vanilla-extract/recipes";
import { theme } from "@/styles/theme";

export const container = style({
  marginBottom: "3rem",
});

export const alert = recipe({
  base: {
    position: "fixed",
    top: 0,
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    width: "100%",
    margin: "auto",
    padding: "0.7rem 1rem",
    marginBottom: "2rem",
    fontWeight: 700,
    fontSize: "1.05rem",
    zIndex: 100,
    textShadow: "0px 4px 4px rgba(0, 0, 0, 0.10)",
    backdropFilter: "saturate(180%) blur(20px)",
  },
  variants: {
    variant: {
      primary: {
        color: "white",
        background: theme.colors.primary.deco(10),
      },
      warning: {
        color: "white",
        background: theme.colors.warning.deco(10),
      },
      error: {
        color: "white",
        background: theme.colors.error.deco(10),
      },
    },
  },
});

export const content = style({
  zIndex: 1,
  display: "flex",
  justifyContent: "center",
  alignItems: "center",
  gap: 4,
});

export const animator = style({ zIndex: 0 });
