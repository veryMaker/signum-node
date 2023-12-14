import { style } from "@vanilla-extract/css";
import { recipe } from "@vanilla-extract/recipes";
import { theme } from "@/styles/theme";

export const card = style({
  width: "25%",
  display: "flex",
  flexDirection: "column",
  justifyContent: "center",
  alignItems: "center",
  position: "relative",
  "@media": {
    "(max-width: 600px)": {
      width: "47%",
      marginBottom: "1rem",
    },
  },
});

export const title = style({
  color: theme.colors.primary.deco(50),
  textAlign: "center",
  fontWeight: 600,
  zIndex: 2,
  margin: 0,
});

export const value = recipe({
  base: {
    margin: 0,
    fontSize: "1.4rem",
    fontWeight: "bold",
    textAlign: "center",
  },
  variants: {
    variant: {
      error: { color: theme.colors.warning.deco(50) },
      success: { color: "hsl(152, 39%, 65%)" },
    },
  },
});
