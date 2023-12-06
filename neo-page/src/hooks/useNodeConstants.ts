import { Address } from "@signumjs/core";
import { useStore } from "@/states";
import { useFetchingInterval } from "@/hooks/useFetchingInterval";
import { defaultCashBackId } from "@/types";
import useSWR from "swr";

export const useNodeConstants = () => {
  const interval = useFetchingInterval();

  const network = useStore((state) => state.network);
  const cashBackId = useStore((state) => state.cashBackId);
  const setNetwork = useStore((state) => state.setNetwork);
  const setCashbackId = useStore((state) => state.setCashbackId);

  useSWR(
    interval ? `getConstants` : null,
    async () => {
      try {
        const response = await fetch("/api?requestType=getConstants");

        if (response.ok) {
          const result = await response.json();
          setNetwork(result.networkName);
          setCashbackId(result.cashBackId);
        }

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } catch (e: any) {
        console.error(e, "Error Fetching");
        return null;
      }
    },
    { dedupingInterval: interval, refreshInterval: interval }
  );

  const cashBackRS = cashBackId
    ? Address.fromNumericId(cashBackId).getReedSolomonAddress()
    : "";
  const isDefaultCashbackIdSet = cashBackId === defaultCashBackId;
  const isTestnet = network === "Signum-TESTNET";
  const isLoading = !network;

  return {
    network,
    isTestnet,
    cashBackId,
    cashBackRS,
    isDefaultCashbackIdSet,
    isLoading,
  };
};
