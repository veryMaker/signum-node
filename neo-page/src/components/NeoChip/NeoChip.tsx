import { FrameSVGNefrex } from "@arwes/react-frames";
import { frame, neoChip } from "./NeoChip.css";

interface Props {
  label: string;
}

export const NeoChip = ({ label }: Props) => {
  return (
    <div className={neoChip}>
      <span>
        <b>{label}</b>
      </span>
      <FrameSVGNefrex className={frame} />
    </div>
  );
};
