import { useSound } from "@/hooks/useSound";
import type { ThirdPartyApp } from "@/types/thirdPartyApp";
import * as classes from "./AppCard.css";

export const AppCard = ({ title, description, img, url }: ThirdPartyApp) => {
  const { playClickSound } = useSound();

  return (
    <a
      href={url}
      onClick={playClickSound}
      target="_blank"
      className={classes.card}
    >
      <img src={img} className={classes.picture} alt={title} />

      <div className={classes.content}>
        <h6 className={classes.title}>{title}</h6>
        <p className={classes.description}>{description}</p>
      </div>
    </a>
  );
};
