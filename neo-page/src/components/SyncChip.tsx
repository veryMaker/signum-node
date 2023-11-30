import { FrameSVGOctagon } from "@arwes/react-frames";
import { Animator } from "@arwes/react-animator";
import { Puffs } from "@arwes/react-bgs";


export const SyncChip = () => {
  return (
    <div
      style={{
        position: "relative",
        zIndex: 0,
        padding: "1rem 2rem",
        marginBottom: "1rem",
        overflow: "hidden",
      }}
    >
      <span
        style={{
          color: "hsl(152, 39%, 50%)",
          fontWeight: 900,
          margin: 0,
          textShadow: "0 0 15px hsl(152, 39%, 50%) ",
          zIndex: 100,
          width: "100%",
          textAlign: "center",
          display: "inline-block",
        }}
      >
        25% Synced
      </span>

      <FrameSVGOctagon
        css={{
          "[data-name=bg]": {
            color: "hsl(152, 39%, 10%)",
          },
          "[data-name=line]": {
            color: "hsl(152, 39%, 50%)",
          },
        }}
      />

      <Animator
        active
        duration={{
          // The duration of an individual animation sequence.
          interval: 1,
        }}
      >
        <div
          style={{
            position: "absolute",
            width: "100%",
            height: "100%",
            top: 0,
            left: 0,
          }}
        >
          {/* Canvas element will ocupy the positioned parent element. */}
          <Puffs color="hsla(180, 100%, 75%, 0.2)" quantity={25} />
        </div>
      </Animator>
    </div>
  );
};
