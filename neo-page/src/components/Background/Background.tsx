import {
  type AppTheme,
  GridLines,
  Dots,
  MovingLines,
  Animator,
} from "@arwes/react";
import * as classes from "./Background.css";

interface Props {
  theme: AppTheme;
}

export const Background = ({ theme }: Props) => {
  const color = theme.colors.primary.deco(1);

  return (
    <Animator duration={{ interval: 10 }}>
      <div className={classes.defaultBG}>
        <GridLines lineColor={color} distance={50} />
        <Dots color={color} distance={50} />
        <MovingLines lineColor={color} distance={50} sets={20} />
      </div>
    </Animator>
  );
};
