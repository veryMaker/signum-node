import { Animator } from "@arwes/react-animator";
import { Puffs } from "@arwes/react-bgs";
import * as classes from "./KpiCard.css";

interface Props {
  title: string;
  value: number;
  isBurning?: boolean;
}

export const KpiCard = ({ title, value, isBurning }: Props) => {
  const formattedValue = new Intl.NumberFormat("en-US").format(value);

  return (
    <div className={classes.card}>
      <span
        className={classes.value({ variant: isBurning ? "error" : "success" })}
      >
        {formattedValue}
      </span>

      <p className={classes.title}>{title}</p>

      {isBurning && (
        <Animator
          active
          duration={{
            interval: 1,
          }}
        >
          <div className={classes.puff}>
            <Puffs color="hsla(40, 81%, 50%, 0.5)" quantity={10} />
          </div>
        </Animator>
      )}
    </div>
  );
};
