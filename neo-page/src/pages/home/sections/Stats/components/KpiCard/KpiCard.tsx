import { formatNumber } from "@/utils/formatNumber";
import { BurnIndicator } from "./components/BurnIndicator";
import { LoadingIndicator } from "./components/LoadingIndicator";
import * as classes from "./KpiCard.css";

interface Props {
  title: string;
  value: number;
  isBurning?: boolean;
  isLoading: boolean;
}

export const KpiCard = ({ title, value, isBurning, isLoading }: Props) => {
  const formattedValue = formatNumber(value);

  return (
    <div className={classes.card}>
      <span
        className={classes.value({ variant: isBurning ? "error" : "success" })}
      >
        {isLoading ? "Loading..." : formattedValue}
      </span>

      <p className={classes.title}>{title}</p>

      {isLoading && <LoadingIndicator />}

      {!!(isBurning && !isLoading) && <BurnIndicator />}
    </div>
  );
};
