import { style } from "@vanilla-extract/css";

export const card = style({
  width: "33%",
  display: "flex",
  padding: "0 1rem",
  gap: 16,
  outline: "none",
  "@media": {
    "(max-width: 1000px)": {
      width: "48%",
      marginBottom: "1.5rem",
    },
    "(max-width: 600px)": {
      width: "100%",
    },
  },
});

export const picture = style({
  width: 75,
  height: 75,
  maxWidth: 75,
  maxHeight: 75,
  objectFit: "cover",
  borderRadius: 6,
  flex: 1,
});

export const content = style({ flex: 1, overflow: "hidden" });

const defaultTextStyle = {
  margin: 0,
  width: "100%",
  overflow: "hidden",
  textOverflow: "ellipsis",
  display: "-webkit-box",
  WebkitLineClamp: 2,
};

export const title = style({
  ...defaultTextStyle,
  WebkitBoxOrient: "vertical",
  fontSize: 16,
});

export const description = style({
  ...defaultTextStyle,
  WebkitBoxOrient: "vertical",
  fontSize: 14,
});
