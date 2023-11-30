import { css } from "@emotion/react";
import { theme } from "@/utils/theme";
import { useSound } from "@/hooks/useSound";

interface Props {
  title: string;
  description: string;
  background: string;
  img?: string;
  initial?: string;
  url: string;
}

export const AppCard = ({
  title,
  description,
  background,
  img,
  initial,
  url,
}: Props) => {
  const { playClickSound } = useSound();

  return (
    <a
      onClick={playClickSound}
      href={`/${url}`}
      target="_blank"
      css={css`
        display: flex;
        flex-direction: column;
        justify-content: flex-start;
        align-items: flex-start;
        padding: 2rem 1rem 1rem;
        width: 32.2%;
        border: 1px dashed rgba(255, 255, 255, 0.3);
        color: ${theme.colors.primary.deco(100)};
        transition: all 0.5s ease;
        outline: none;
        box-shadow: none;
        cursor: pointer;

        h3 {
          margin: 0;
        }

        p {
          font-size: 14px;
        }

        picture {
          width: 95px;
          height: 95px;
          background: ${background};
          display: flex;
          justify-content: center;
          align-items: center;
          margin-top: -4rem;
          margin-bottom: 0.5rem;
          border-radius: 6px;
        }

        picture img {
          width: 90%;
        }

        picture h6 {
          text-align: center;
          color: white;
          font-size: 50px;
          font-weight: 900;
        }

        @media (min-width: 1200px) {
          :hover {
            background: rgba(43, 237, 237, 0.2);
            border-color: rgba(43, 237, 237, 0.3);
            box-shadow: 0px 0px 20px 0px rgba(43, 237, 237, 0.5);
            transform: scale(1.05);
          }
        }

        @media (max-width: 1000px) {
          width: 48%;
          margin-bottom: 1rem;
        }

        @media (max-width: 600px) {
          width: 100%;
          justify-content: center;
          align-items: center;
        }
      `}
    >
      <picture>
        {!!img && <img src={img}  alt={title}/>}
        {!!initial && <h6>{initial}</h6>}
      </picture>

      <h3>{title}</h3>
      <p>{description}</p>
    </a>
  );
};
