import { create } from "zustand";
import { devtools, persist } from "zustand/middleware";
import type { State, Action } from "./types";
import { defaultCashBackId } from "@/types";

// Zustand docs recommends to colocate actions and states within the store
export const useStore = create<State & Action>()(
  devtools(
    persist(
      (set) => ({
        network: "Signum",
        version: "",
        cashBackId: defaultCashBackId,
        numberOfBlocks: 0,
        lastBlockchainFeederHeight: 0,
        numberOfTransactions: 0,
        numberOfATs: 0,
        numberOfAssets: 0,
        numberOfAliases: 0,
        numberOfSubscriptions: 0,
        numberOfSubscriptionPayments: 0,
        burnedFunds: 0,
        circulatingFunds: 0,
        thirdPartyApps: { mainnet: [], testnet: [] },
        setNetwork: (value) => set(() => ({ network: value })),
        setVersion: (value) => set(() => ({ version: value })),
        setCashbackId: (value) => set(() => ({ cashBackId: value })),
        setNumberOfBlocks: (value) => set(() => ({ numberOfBlocks: value })),
        setLastBlockchainFeederHeight: (value) =>
          set(() => ({ lastBlockchainFeederHeight: value })),
        setNumberOfTransactions: (value) =>
          set(() => ({ numberOfTransactions: value })),
        setNumberOfATs: (value) => set(() => ({ numberOfATs: value })),
        setNumberOfAssets: (value) => set(() => ({ numberOfAssets: value })),
        setNumberOfAliases: (value) => set(() => ({ numberOfAliases: value })),
        setNumberOfSubscriptions: (value) =>
          set(() => ({ numberOfSubscriptions: value })),
        setNumberOfSubscriptionPayments: (value) =>
          set(() => ({ numberOfSubscriptionPayments: value })),
        setBurnedFunds: (value) => set(() => ({ burnedFunds: value })),
        setCirculatingFunds: (value) =>
          set(() => ({ circulatingFunds: value })),
        setThirdPartyApps: (value) => set(() => ({ thirdPartyApps: value })),
      }),
      {
        name: "app-storage",
        version: 1,
      }
    )
  )
);
