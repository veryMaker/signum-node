import { style } from "@vanilla-extract/css";

export const title = style({
  margin: 0,
  fontSize: "1.5rem",
  textAlign: "center",
  "@media": {
    "(max-width: 600px)": {
      fontSize: "1.4rem",
    },
  },
});

export const description = style({
  fontWeight: "bold",
  fontSize: "1.2rem",
  textAlign: "center",
  "@media": {
    "(max-width: 600px)": {
      fontSize: "1rem",
    },
  },
});
