import { FrameSVGOctagon } from "@arwes/react-frames";
import { frame, heightChip } from "./HeightChip.css";

export const HeightChip = () => {
  return (
    <div className={heightChip}>
      <span>
        <b>Height: 000,000,000</b>
      </span>
      <FrameSVGOctagon className={frame} />
    </div>
  );
};
