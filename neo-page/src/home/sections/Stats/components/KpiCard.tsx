import { css } from "@emotion/react";
import { theme } from "@/utils/theme";

interface Props {
  title: string;
  value: number;
}

export const KpiCard = ({ title, value }: Props) => {
  const formattedValue = new Intl.NumberFormat("en-US").format(value);

  return (
    <div
      css={css`
        width: 25%;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;

        span {
          margin: 0;
          color: ${theme.colors.warning.deco(50)};
          font-size: 1.4rem;
          font-weight: bold;
          text-align: center;
        }

        p {
          color: ${theme.colors.primary.deco(50)};
          text-align: center;
          font-weight: 500;
        }

        @media (max-width: 600px) {
          width: 47%;
          margin-bottom: 1rem;
        }
      `}
    >
      <span>{formattedValue}</span>
      <p>{title}</p>
    </div>
  );
};
