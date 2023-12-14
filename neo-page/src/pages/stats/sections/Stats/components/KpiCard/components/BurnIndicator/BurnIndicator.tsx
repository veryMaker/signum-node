import { Animator } from "@arwes/react-animator";
import { Puffs } from "@arwes/react-bgs";
import * as classes from "./BurnIndicator.css";

export const BurnIndicator = () => (
  <Animator
    active
    duration={{
      interval: 1,
    }}
  >
    <div className={classes.container}>
      <Puffs color="hsla(40, 81%, 50%, 0.5)" quantity={10} />
    </div>
  </Animator>
);
