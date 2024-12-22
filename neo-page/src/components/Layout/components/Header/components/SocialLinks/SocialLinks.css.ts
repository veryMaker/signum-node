import { style } from "@vanilla-extract/css";
import { theme } from "@/styles/theme";

export const contentContainer = style({
  display: "flex",
  alignItems: "center",
  gap: 16,
  zIndex: 1,
});

export const avatar = style({
  fontSize: 26,
  color: theme.colors.primary.deco(100),
  outline: "none",
});
