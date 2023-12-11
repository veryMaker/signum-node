import { style } from "@vanilla-extract/css";
import { theme } from "@/styles/theme";

export const container = style({
  display: "flex",
  flexDirection: "column",
  justifyContent: "center",
  alignItems: "center",
});

export const title = style({
  color: "white",
  fontSize: "2rem",
  textAlign: "center",
});

export const btnContainer = style({
  display: "flex",
  justifyContent: "center",
  alignItems: "center",
  gap: 8,
  marginTop: "0.5rem",
  marginBottom: "2rem",
});

export const button = style({
  color: theme.colors.primary.deco(100),
  background: theme.colors.primary.deco(5),
  fontWeight: "bold",
  padding: "0.5rem",
  borderRadius: 4,
  fontSize: "1.1rem",
  cursor: "pointer",
});
