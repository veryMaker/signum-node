import { style } from "@vanilla-extract/css";

export const titleContainer = style({
  display: "flex",
  justifyContent: "space-between",
  alignItems: "center",
  "@media": {
    "(max-width: 600px)": {
      flexDirection: "column-reverse",
      justifyContent: "center",
      alignItems: "stretch",
      gap: "1rem",
    },
  },
});

export const cardsContainer = style({
  display: "flex",
  justifyContent: "flex-start",
  alignItems: "center",
  flexWrap: "wrap",
  gap: "4rem 1rem",
  marginTop: "3rem",
});
