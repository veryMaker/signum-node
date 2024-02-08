import { style } from "@vanilla-extract/css";

export const contentContainer = style({
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  padding: "1rem",
  "@media": {
    "(max-width: 600px)": {
      flexDirection: "column-reverse",
      textAlign: "center",
    },
  },
});

export const contentSection = style({
  width: "60%",
  "@media": {
    "(max-width: 600px)": {
      width: "100%",
    },
  },
});

export const spatialContainer = style({
  width: "40%",
  height: 300,
  "@media": {
    "(max-width: 600px)": {
      width: "100%",
      height: 250,
    },
  },
});

export const title = style({
  margin: 0,
  fontSize: "2.5rem",
  "@media": {
    "(max-width: 600px)": {
      fontSize: "1.4rem",
    },
  },
});

export const description = style({
  margin: 0,
  fontWeight: "bold",
  fontSize: "1.5rem",
  "@media": {
    "(max-width: 600px)": {
      fontSize: "1rem",
    },
  },
});
