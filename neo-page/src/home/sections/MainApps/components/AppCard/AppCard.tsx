import { useSound } from "@/hooks/useSound";
import * as classes from "./AppCard.css";

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
      className={classes.card}
    >
      <picture className={classes.picture} style={{ background }}>
        {!!img && <img src={img} alt={title} className={classes.pictureImg} />}
        {!!initial && <h6 className={classes.pictureLetters}>{initial}</h6>}
      </picture>

      <h3 className={classes.title}>{title}</h3>
      <p className={classes.description}>{description}</p>
    </a>
  );
};
