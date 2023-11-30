import { style } from "@vanilla-extract/css";
import { defaultMaxWidth } from "@/types";

export const defaultContainer = style({
  width: "100%",
  maxWidth: defaultMaxWidth,
  margin: "auto",
  display: "flex",
  flexDirection: "column",
  padding: "1rem",
  marginBottom: "2rem",
});

export const defaultCardContainer = style({
  display: "flex",
  flexDirection: "row",
  alignItems: "center",
  flexWrap: "wrap",
  gap: "2rem 0rem",
  marginTop: "2rem",
  "@media": {
    "(max-width: 600px)": {
      justifyContent: "center",
      gap: "1rem",
    },
  },
});
