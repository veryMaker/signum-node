import { css } from "@emotion/react";
import { useSound } from "@/hooks/useSound";
import type { ThirdPartyApp } from "../types";

export const AppCard = ({
  title,
  description,
  imgSrc,
  href,
}: ThirdPartyApp) => {
  const { playClickSound } = useSound();

  return (
    <a
      href={href}
      onClick={playClickSound}
      target="_blank"
      css={css`
        width: 33%;
        display: flex;
        padding: 1rem;
        gap: 16px;
        outline: none;

        picture {
          min-width: 75px;
          max-width: 75px;
          min-height: 75px;
          max-height: 75px;
          background: url(${imgSrc});
          background-size: cover;
          background-position: center;
          border-radius: 6px;
          flex: 1;
        }

        .content {
          flex: 1;
          overflow: hidden;
        }

        .content h6,
        .content p {
          margin: 0;
          width: 100%;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }

        .content h6 {
          font-size: 16px;
        }

        .content p {
          font-size: 14px;
          margin: 0;
        }
      `}
    >
      <picture />

      <div className="content">
        <h6>{title}</h6>
        <p>{description}</p>
      </div>
    </a>
  );
};
