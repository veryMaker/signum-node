import { Fragment } from "react";
import { type AppTheme, Dots, MovingLines, Animator } from "@arwes/react";
import { Illuminator } from "@arwes/react-frames";
import * as classes from "./Background.css";

interface Props {
  theme: AppTheme;
}

export const Background = ({ theme }: Props) => {
  return (
    <Fragment>
      <Animator duration={{ interval: 10 }}>
        <div className={classes.defaultBG}>
          <Dots color={theme.colors.primary.deco(1)} distance={50} />
          <MovingLines
            lineColor={theme.colors.primary.deco(3)}
            distance={50}
            sets={20}
          />
        </div>
      </Animator>

      <div className={classes.illuminatorContainer}>
        <Illuminator
          color="hsl(180 50% 50% / 20%)"
          size={82}
          className={classes.illuminator}
        />

        <svg className={classes.svgEffect}></svg>
      </div>
    </Fragment>
  );
};
