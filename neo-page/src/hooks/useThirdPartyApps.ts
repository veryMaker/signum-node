import { useStore } from "@/states";
import useSWR from "swr";

export const useThirdPartyApps = () => {
  const interval = 240_000;

  const thirdPartyApps = useStore((state) => state.thirdPartyApps);
  const setThirdPartyApps = useStore((state) => state.setThirdPartyApps);

  useSWR(
    `getThirdPartyApps`,
    async () => {
      try {
        const response = await fetch(
          "https://signum-network.github.io/public-resources/third-party-apps.json"
        );

        if (response.ok) {
          const result = await response.json();
          setThirdPartyApps(result);
        }

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } catch (e: any) {
        console.error(e, "Error Fetching third party apps");
        return null;
      }
    },
    { dedupingInterval: interval, refreshInterval: interval }
  );

  const isLoading = !thirdPartyApps.mainnet.length;

  return { thirdPartyApps, isLoading };
};
