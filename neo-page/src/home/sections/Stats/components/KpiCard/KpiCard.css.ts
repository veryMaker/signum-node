import { style } from "@vanilla-extract/css";
import { theme } from "@/styles/theme";

export const card = style({
  width: "25%",
  display: "flex",
  flexDirection: "column",
  justifyContent: "center",
  alignItems: "center",
  "@media": {
    "(max-width: 600px)": {
      width: "47%",
      marginBottom: "1rem",
    },
  },
});

export const value = style({
  margin: 0,
  color: theme.colors.warning.deco(50),
  fontSize: "1.4rem",
  fontWeight: "bold",
  textAlign: "center",
});

export const title = style({
  color: theme.colors.primary.deco(50),
  textAlign: "center",
  fontWeight: 600,
});
