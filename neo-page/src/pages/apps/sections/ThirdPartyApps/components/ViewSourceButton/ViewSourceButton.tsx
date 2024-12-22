import { FrameSVGOctagon } from "@arwes/react-frames";
import { frame, chip } from "./ViewSourceButton.css";
import { useSound } from "@/hooks/useSound";

export const ViewSourceButton = () => {
  const { playClickSound } = useSound();

  return (
    <a
      href="https://signum-network.github.io/public-resources/"
      target="_blank"
      className={chip}
      onClick={playClickSound}
    >
      <span>View Source</span>
      <FrameSVGOctagon className={frame} />
    </a>
  );
};
