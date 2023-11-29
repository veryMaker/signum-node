import { css } from "@emotion/react";
import { theme } from "@/utils/theme";
import { FaLink } from "react-icons/fa6";
import { useSound } from "@/hooks/useSound";

interface Props {
  title: string;
  description: string;
  href: string;
}

export const WikiCard = ({ title, description, href }: Props) => {
  const { playClickSound } = useSound();

  return (
    <a
      href={href}
      onClick={playClickSound}
      target="_blank"
      css={css`
        width: 24%;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        padding: 2rem 1rem;
        border: 1px dashed;
        border-color: rgba(255, 255, 255, 0.3);
        color: ${theme.colors.primary.deco(100)};
        transition: all 0.5s ease;
        outline: none;
        box-shadow: none;
        cursor: pointer;
        color: ${theme.colors.primary.deco(50)};
        text-align: center;

        .icon {
          font-size: 2rem;
        }

        span {
          margin: 0;
          font-size: 1.1rem;
          font-weight: bold;
          text-align: center;
        }

        @media (min-width: 1200px) {
          :hover {
            background: rgba(43, 237, 237, 0.2);
            border-color: rgba(43, 237, 237, 0.3);
            box-shadow: 0px 0px 20px 0px rgba(43, 237, 237, 0.5);
            transform: scale(1.05);
          }
        }

        @media (max-width: 600px) {
          width: 47%;
          margin-bottom: 1rem;
        }
      `}
    >
      <span className="icon">
        <FaLink />
      </span>

      <span>{title}</span>
      <p>{description}</p>
    </a>
  );
};
