import { useSound } from "@/hooks/useSound";
import * as classes from "./AppCard.css";

interface Props {
  title: string;
  secondTitle?: string;
  description: string;
  background: string;
  img?: string;
  initial?: string;
  url: string;
}

export const AppCard = ({
  title,
  secondTitle,
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
      className={classes.card}
    >
      <picture className={classes.picture} style={{ background }}>
        {!!img && <img src={img} alt={title} className={classes.pictureImg} />}
        {!!initial && <h6 className={classes.pictureLetters}>{initial}</h6>}
      </picture>

      <div className={classes.titleContainer}>
        <h3 className={classes.title}>{title}</h3>

        {!!secondTitle && (
          <span className={classes.secondTitle}>{secondTitle}</span>
        )}
      </div>

      <p className={classes.description}>{description}</p>
    </a>
  );
};
