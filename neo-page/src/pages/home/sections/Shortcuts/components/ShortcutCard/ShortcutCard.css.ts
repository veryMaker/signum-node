import { style } from "@vanilla-extract/css";
import { recipe } from "@vanilla-extract/recipes";
import { theme } from "@/styles/theme";

export const card = recipe({
  base: {
    width: "100%",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    background: "rgba(43, 237, 237, 0.2)",
    border: "1px dashed rgba(43, 237, 237, 0.3)",
    marginBottom: "2rem",
    padding: "2rem",
    color: theme.colors.primary.deco(100),
    boxShadow: "none",
    transition: "all 0.5s ease",
    ":hover": {
      boxShadow: "0px 0px 20px 0px rgba(43, 237, 237, 0.5)",
    },
    "@media": {
      "(max-width: 600px)": {
        flexDirection: "column",
      },
    },
  },
  variants: {
    variant: {
      left: {
        flexDirection: "row",
      },
      right: {
        flexDirection: "row-reverse",
      },
    },
  },
});

export const pictureContainer = style({
  width: "25%",
  "@media": {
    "(max-width: 600px)": {
      width: "100%",
      marginBottom: "1rem",
      maxHeight: 200,
      overflow: "hidden",
    },
  },
});

export const img = style({
  width: "100%",
  objectFit: "contain",
});

export const contentContainer = style({
  width: "70%",
  display: "flex",
  justifyContent: "flex-start",
  alignItems: "flex-start",
  flexWrap: "wrap",
  padding: "0 0.5rem",
  columnGap: 8,
  "@media": {
    "(max-width: 600px)": {
      width: "100%",
    },
  },
});

export const contentTitle = style({
  width: "100%",
  textAlign: "left",
});

export const contentColumn = style({
  width: "24%",
  display: "flex",
  flexDirection: "column",
  "@media": {
    "(max-width: 600px)": {
      width: "100%",
      marginBottom: "0.5rem",
    },
  },
});

export const columnTitle = style({
  margin: 0,
  fontSize: "1.2rem",
});

export const columnDescription = style({
  fontSize: 14,
  marginBottom: "0.5rem",
  "@media": {
    "(max-width: 600px)": {
      margin: 0,
    },
  },
});
