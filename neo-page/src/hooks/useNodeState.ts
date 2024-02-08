import { useStore } from "@/states";
import { useFetchingInterval } from "@/hooks/useFetchingInterval";
import { Amount } from "@signumjs/util";
import useSWR from "swr";

export const useNodeState = () => {
  const interval = useFetchingInterval();

  const numberOfTransactions = useStore((state) => state.numberOfTransactions);
  const numberOfATs = useStore((state) => state.numberOfATs);
  const numberOfAssets = useStore((state) => state.numberOfAssets);
  const numberOfAliases = useStore((state) => state.numberOfAliases);
  const numberOfSubscriptions = useStore(
    (state) => state.numberOfSubscriptions
  );
  const numberOfSubscriptionPayments = useStore(
    (state) => state.numberOfSubscriptionPayments
  );
  const burnedFunds = useStore((state) => state.burnedFunds);
  const circulatingFunds = useStore((state) => state.circulatingFunds);
  const setNumberOfTransactions = useStore(
    (state) => state.setNumberOfTransactions
  );
  const setNumberOfATs = useStore((state) => state.setNumberOfATs);
  const setNumberOfAssets = useStore((state) => state.setNumberOfAssets);
  const setNumberOfAliases = useStore((state) => state.setNumberOfAliases);
  const setNumberOfSubscriptions = useStore(
    (state) => state.setNumberOfSubscriptions
  );
  const setNumberOfSubscriptionPayments = useStore(
    (state) => state.setNumberOfSubscriptionPayments
  );
  const setBurnedFunds = useStore((state) => state.setBurnedFunds);
  const setCirculatingFunds = useStore((state) => state.setCirculatingFunds);

  useSWR(
    interval ? `getState` : null,
    async () => {
      try {
        const response = await fetch("/api?requestType=getState");

        if (response.ok) {
          const result = await response.json();

          setNumberOfTransactions(result.numberOfTransactions);
          setNumberOfATs(result.numberOfATs);
          setNumberOfAssets(result.numberOfAssets);
          setNumberOfAliases(result.numberOfAliases);
          setNumberOfSubscriptions(result.numberOfSubscriptions);
          setNumberOfSubscriptionPayments(result.numberOfSubscriptionPayments);

          setBurnedFunds(
            Number(Amount.fromPlanck(result.totalBurntNQT).getSigna())
          );
          setCirculatingFunds(
            Number(Amount.fromPlanck(result.circulatingSupplyNQT).getSigna())
          );
        }

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } catch (e: any) {
        console.error(e, "Error Fetching");
        return null;
      }
    },
    { dedupingInterval: interval, refreshInterval: interval }
  );

  const isLoading = !numberOfTransactions;

  return {
    numberOfTransactions,
    numberOfATs,
    numberOfAssets,
    numberOfAliases,
    numberOfSubscriptions,
    numberOfSubscriptionPayments,
    burnedFunds,
    circulatingFunds,
    isLoading,
  };
};
