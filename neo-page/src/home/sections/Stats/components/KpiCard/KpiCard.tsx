import * as classes from "./KpiCard.css";

interface Props {
  title: string;
  value: number;
}

export const KpiCard = ({ title, value }: Props) => {
  const formattedValue = new Intl.NumberFormat("en-US").format(value);

  return (
    <div className={classes.card}>
      <span className={classes.value}>{formattedValue}</span>
      <p className={classes.title}>{title}</p>
    </div>
  );
};
