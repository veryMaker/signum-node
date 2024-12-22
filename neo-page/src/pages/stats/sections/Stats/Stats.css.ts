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

export const contentContainer = style({
  display: "flex",
  alignItems: "center",
  gap: 8,
  justifyContent: "space-between",
});

export const genesisText = style({
  color: "hsl(152, 39%, 60%)",
});

export const syncChip = style({
  display: "none",
  "@media": {
    "(max-width: 600px)": {
      display: "inline-block",
    },
  },
});
