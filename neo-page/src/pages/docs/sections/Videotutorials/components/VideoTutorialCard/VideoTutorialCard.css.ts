import { style } from "@vanilla-extract/css";
import { theme } from "@/styles/theme";

export const card = style({
  width: "32%",
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  padding: "2rem 1rem",
  border: "1px dashed",
  borderColor: "rgba(255, 255, 255, 0.3)",
  color: theme.colors.primary.deco(50),
  transition: "all 0.5s ease",
  outline: "none",
  boxShadow: "none",
  cursor: "pointer",
  textAlign: "center",
  ":hover": {
    background: "rgba(43, 237, 237, 0.2)",
    borderColor: "rgba(43, 237, 237, 0.3)",
    boxShadow: "0px 0px 20px 0px rgba(43, 237, 237, 0.5)",
    transform: "scale(1.05)",
  },
  "@media": {
    "(max-width: 600px)": {
      width: "47%",
      marginBottom: "1rem",
    },
  },
});

export const icon = style({ fontSize: "2rem", color: "#FF0000" });

export const title = style({
  margin: 0,
  fontSize: "1.1rem",
  fontWeight: "bold",
  textAlign: "center",
  paddingBottom: "0.5rem",
  marginBottom: "0.5rem",
  borderBottom: "1px dashed rgba(255, 255, 255, 0.3)",
  width: "100%",
});
