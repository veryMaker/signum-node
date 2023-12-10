import { style } from "@vanilla-extract/css";
import { theme } from "@/styles/theme";

export const card = style({
  display: "flex",
  flexDirection: "column",
  justifyContent: "flex-start",
  alignItems: "flex-start",
  padding: "2rem 1rem 1rem",
  width: "32.2%",
  border: "1px dashed rgba(255, 255, 255, 0.3)",
  color: theme.colors.primary.deco(100),
  transition: "all 0.5s ease",
  outline: "none",
  boxShadow: "none",
  cursor: "pointer",
  ":hover": {
    background: "rgba(43, 237, 237, 0.2)",
    borderColor: "rgba(43, 237, 237, 0.3)",
    boxShadow: "0px 0px 20px 0px rgba(43, 237, 237, 0.5)",
    transform: "scale(1.05)",
  },
  "@media": {
    "(max-width: 1000px)": {
      width: "48%",
      marginBottom: "1rem",
    },
    "(max-width: 600px)": {
      width: "100%",
      justifyContent: "center",
      alignItems: "center",
      marginBottom: "2rem",
    },
  },
});

export const titleContainer = style({
  display: "flex",
  alignItems: "center",
  gap: 8,
});

export const title = style({ margin: 0 });

export const secondTitle = style({
  margin: 0,
  padding: "0 12px",
  color: theme.colors.secondary.deco(100),
  background: theme.colors.secondary.deco(5),
  fontSize: 12,
  borderRadius: 8,
});

export const description = style({ fontSize: 14 });

export const picture = style({
  width: 95,
  height: 95,
  display: "flex",
  justifyContent: "center",
  alignItems: "center",
  marginTop: "-4rem",
  marginBottom: "0.5rem",
  borderRadius: 6,
});

export const pictureImg = style({
  width: "90%",
});

export const pictureLetters = style({
  textAlign: "center",
  color: "white",
  fontSize: 50,
  fontWeight: 900,
});
