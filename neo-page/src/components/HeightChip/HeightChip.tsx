import { FrameSVGOctagon } from "@arwes/react-frames";
import { useBlockchainStatus } from "@/hooks/useBlockchainStatus";
import { formatNumber } from "@/utils/formatNumber";
import { frame, chip } from "./HeightChip.css";

export const HeightChip = () => {
  const { numberOfBlocks, isLoading } = useBlockchainStatus();
  const formattedValue = formatNumber(numberOfBlocks);

  return (
    <div className={chip}>
      <span>
        {isLoading && <b>Loading...</b>}
        {!isLoading && <b>Height: {formattedValue}</b>}
      </span>
      <FrameSVGOctagon className={frame} />
    </div>
  );
};
