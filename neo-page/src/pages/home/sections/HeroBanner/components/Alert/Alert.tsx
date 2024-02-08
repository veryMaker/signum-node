import { FrameSVGCorners } from "@arwes/react-frames";
import { useSound } from "@/hooks/useSound";
import { defaultContainer } from "@/styles/containers.css";
import * as classes from "./Alert.css";

interface Props {
  show: boolean;
  title: string;
  href: string;
  cta: string;
}

export const Alert = ({ show, title, href, cta }: Props) => {
  const { playClickSound } = useSound();

  if (!show) return null;

  return (
    <section className={defaultContainer}>
      <div className={classes.banner}>
        {title}
        <a
          onClick={playClickSound}
          className={classes.button}
          href={href}
          target="_blank"
        >
          {cta}
          <FrameSVGCorners className={classes.buttonFrame} />
        </a>
        <FrameSVGCorners className={classes.frame} />
      </div>
    </section>
  );
};
