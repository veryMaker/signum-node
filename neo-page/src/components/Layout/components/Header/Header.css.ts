import { style } from "@vanilla-extract/css";
import { defaultMaxWidth } from "@/types";

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

export const navLinksContainer = style({
  "@media": {
    "(max-width: 600px)": {
      order: 3,
    },
  },
});

export const socialLinksContainer = style({
  "@media": {
    "(max-width: 600px)": {
      order: 2,
      marginBottom: "1rem",
    },
  },
});
