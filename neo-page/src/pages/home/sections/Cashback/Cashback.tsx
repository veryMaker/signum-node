import { FrameSVGCorners } from "@arwes/react-frames";
import { useSound } from "@/hooks/useSound";
import { useNodeConstants } from "@/hooks/useNodeConstants";
import { defaultContainer } from "@/styles/containers.css";
import * as classes from "./Cashback.css";

export const Cashback = () => {
  const { isDefaultCashbackIdSet } = useNodeConstants();
  const { playClickSound } = useSound();

  if (!isDefaultCashbackIdSet) return null;

  return (
    <section className={defaultContainer}>
      <div className={classes.banner}>
        Get 25% Cashback from your node fees
        <a
          onClick={playClickSound}
          className={classes.button}
          href="https://docs.signum.network/signum/activate-cashback"
          target="_blank"
        >
          Learn More
          <FrameSVGCorners className={classes.buttonFrame} />
        </a>
        <FrameSVGCorners className={classes.frame} />
      </div>
    </section>
  );
};
