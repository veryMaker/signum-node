import { FrameSVGOctagon } from "@arwes/react-frames";
import { Animator } from "@arwes/react-animator";
import { Puffs } from "@arwes/react-bgs";
import { useBlockchainStatus } from "@/hooks/useBlockchainStatus";
import * as classes from "./SyncChip.css";

export const SyncChip = () => {
  const { syncProgress, isLoading } = useBlockchainStatus();

  const puffQuantity = syncProgress >= 100 ? 5 : 25;

  return (
    <div className={classes.container}>
      <span className={classes.chip}>
        {!isLoading ? `${syncProgress}% Synced` : "Loading..."}
      </span>

      <FrameSVGOctagon className={classes.frame} />

      <Animator
        active
        duration={{
          // The duration of an individual animation sequence.
          interval: 1,
        }}
      >
        <div className={classes.puff}>
          {/* Canvas element will ocupy the positioned parent element. */}
          <Puffs color="hsla(180, 100%, 75%, 0.2)" quantity={puffQuantity} />
        </div>
      </Animator>
    </div>
  );
};
