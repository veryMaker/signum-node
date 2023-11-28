import { FrameSVGNefrex } from "@arwes/react-frames";

interface Props {
  label: string;
}

export const NeoChip = ({ label }: Props) => {
  return (
    <div
      style={{
        position: "relative",
        zIndex: 0,
        padding: "1rem 2rem",
        marginBottom: "1rem",
        display: "flex",
        justifyContent: "center",
      }}
    >
      <span>
        <b>{label}</b>
      </span>

      <FrameSVGNefrex
        css={{
          "[data-name=bg]": {
            color: "hsl(180, 75%, 10%)",
          },
          "[data-name=line]": {
            color: "hsl(180, 75%, 50%)",
          },
        }}
      />
    </div>
  );
};
