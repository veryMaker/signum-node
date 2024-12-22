import { FaYoutube } from "react-icons/fa6";
import { useSound } from "@/hooks/useSound";
import * as classes from "./VideoTutorialCard.css";

interface Props {
  title: string;
  href: string;
}

export const VideoTutorialCard = ({ title, href }: Props) => {
  const { playClickSound } = useSound();

  return (
    <a
      href={href}
      onClick={playClickSound}
      target="_blank"
      className={classes.card}
    >
      <span className={classes.icon}>
        <FaYoutube />
      </span>

      <span className={classes.title}>{title}</span>

      <p>
        <b>{"View Video >"}</b>
      </p>
    </a>
  );
};
