import { style } from "@vanilla-extract/css";

export const titleContainer = style({
  display: "flex",
  justifyContent: "space-between",
});

export const loadingContainer = style({
  position: "relative",
  height: "100vh",
  maxHeight: 500,
  marginTop: 25,
});
