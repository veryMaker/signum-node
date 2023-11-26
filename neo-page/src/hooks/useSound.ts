import { useBleeps } from "@arwes/react";

type BleepsNames = "click" | "intro";

export const useSound = () => {
  const bleeps = useBleeps<BleepsNames>();
  const playClickSound = () => bleeps.click?.play();
  const playIntroSound = () => bleeps.intro?.play();

  return { playClickSound, playIntroSound };
};
