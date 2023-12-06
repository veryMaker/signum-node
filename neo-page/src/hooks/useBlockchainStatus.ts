import { useStore } from "@/states";
import { useFetchingInterval } from "@/hooks/useFetchingInterval";
import useSWR from "swr";

export const useBlockchainStatus = () => {
  const interval = useFetchingInterval();

  const version = useStore((state) => state.version);
  const numberOfBlocks = useStore((state) => state.numberOfBlocks);
  const lastBlockchainFeederHeight = useStore(
    (state) => state.lastBlockchainFeederHeight
  );
  const setVersion = useStore((state) => state.setVersion);
  const setNumberOfBlocks = useStore((state) => state.setNumberOfBlocks);
  const setLastBlockchainFeederHeight = useStore(
    (state) => state.setLastBlockchainFeederHeight
  );

  useSWR(
    interval ? `getBlockchainStatus` : null,
    async () => {
      try {
        const response = await fetch(
          "https://latam.signum.network/api?requestType=getBlockchainStatus"
        );

        if (response.ok) {
          const result = await response.json();
          setVersion(result.version);
          setNumberOfBlocks(result.numberOfBlocks);
          setLastBlockchainFeederHeight(result.lastBlockchainFeederHeight);
        }

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } catch (e: any) {
        console.error(e, "Error Fetching");
        return null;
      }
    },
    { dedupingInterval: interval, refreshInterval: interval }
  );

  const isLoading = !numberOfBlocks;

  const syncProgress =
    numberOfBlocks && lastBlockchainFeederHeight
      ? ((numberOfBlocks - 1) / lastBlockchainFeederHeight) * 100
      : 0;

  return {
    version,
    numberOfBlocks,
    lastBlockchainFeederHeight,
    syncProgress,
    isLoading,
  };
};
