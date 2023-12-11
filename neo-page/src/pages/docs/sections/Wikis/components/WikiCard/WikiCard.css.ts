import { style } from "@vanilla-extract/css";
import { theme } from "@/styles/theme";

export const card = style({
  width: "24%",
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

export const icon = style({ fontSize: "2rem" });

export const title = style({
  margin: 0,
  fontSize: "1.1rem",
  fontWeight: "bold",
  textAlign: "center",
});
