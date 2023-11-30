import { style } from "@vanilla-extract/css";
import { theme } from "@/styles/theme";

export const titleContainer = style({
  display: "flex",
  alignItems: "center",
  gap: "1rem",
});

export const genesisText = style({
  color: theme.colors.warning.deco(30),
});
