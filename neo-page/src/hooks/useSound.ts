import { useBleeps } from "@arwes/react";
import type { BleepsNames } from "@/types";

export const useSound = () => {
  const bleeps = useBleeps<BleepsNames>();
  const playClickSound = () => bleeps.click?.play();
  const playIntroSound = () => bleeps.intro?.play();

  return { playClickSound, playIntroSound };
};
