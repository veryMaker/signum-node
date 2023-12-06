import { useState, useEffect } from "react";
import { Animator } from "@arwes/react-animator";
import { Dots } from "@arwes/react-bgs";

export const LoadingIndicator = () => {
  const [active, setActive] = useState(true);

  useEffect(() => {
    const iid = setInterval(() => setActive((active) => !active), 1200);
    return () => clearInterval(iid);
  }, []);

  return (
    <Animator active={active} duration={{ enter: 0.75, exit: 0.75 }}>
      <Dots
        color="hsla(120, 100%, 75%, 0.1)"
        distance={50}
        size={40}
        origin="top"
      />
    </Animator>
  );
};
