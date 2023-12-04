import { FaLink } from "react-icons/fa6";
import { useSound } from "@/hooks/useSound";
import * as classes from "./WikiCard.css";

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
      className={classes.card}
    >
      <span className={classes.icon}>
        <FaLink />
      </span>

      <span className={classes.title}>{title}</span>
      <p>{description}</p>
    </a>
  );
};
