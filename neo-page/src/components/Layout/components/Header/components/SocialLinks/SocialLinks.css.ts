import { style } from "@vanilla-extract/css";
import { theme } from "@/styles/theme";

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
