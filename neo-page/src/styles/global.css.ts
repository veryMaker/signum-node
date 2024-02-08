import { globalStyle } from "@vanilla-extract/css";
import { createAppStylesBaseline } from "@arwes/react";
import { theme } from "./theme";

const stylesBaseline = createAppStylesBaseline(theme);

Object.keys(stylesBaseline).forEach((styleName) => {
  globalStyle(styleName, stylesBaseline[styleName]);
});
