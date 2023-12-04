import { style } from "@vanilla-extract/css";
import { defaultMaxWidth } from "@/types";
import { theme } from "@/styles/theme";

export const headerContainer = style({
  position: "sticky",
  overflow: "hidden",
  display: "flex",
  justifyContent: "space-between",
  alignItems: "center",
  padding: "1rem",
  width: "100%",
  maxWidth: defaultMaxWidth,
  margin: "auto",
  marginBottom: "1rem",
  flexWrap: "wrap",
  "@media": {
    "(max-width: 600px)": {
      justifyContent: "center",
      gap: "1rem",
    },
  },
});

export const contentContainer = style({
  display: "flex",
  alignItems: "center",
  gap: 16,
  zIndex: 1,
});

export const versionTag = style({
  color: theme.colors.primary.deco(100),
  background: theme.colors.primary.deco(5),
  fontWeight: "bold",
  padding: "0.5rem 1rem",
  borderRadius: 4,
});

export const avatar = style({
  fontSize: 26,
  color: theme.colors.primary.deco(100),
  outline: "none",
});
