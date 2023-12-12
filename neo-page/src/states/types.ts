import { Networks, ThirdPartyAppStorage } from "@/types";

export type State = {
  network: Networks;
  version: string;
  cashBackId: string;
  numberOfBlocks: number;
  lastBlockchainFeederHeight: number;
  numberOfTransactions: number;
  numberOfATs: number;
  numberOfAssets: number;
  numberOfAliases: number;
  numberOfSubscriptions: number;
  numberOfSubscriptionPayments: number;
  burnedFunds: number;
  circulatingFunds: number;
  thirdPartyApps: ThirdPartyAppStorage;
  isXTWalletDetected: boolean;
};

export type Action = {
  setNetwork: (value: Networks) => void;
  setVersion: (value: string) => void;
  setCashbackId: (value: string) => void;
  setNumberOfBlocks: (value: number) => void;
  setLastBlockchainFeederHeight: (value: number) => void;
  setNumberOfTransactions: (value: number) => void;
  setNumberOfATs: (value: number) => void;
  setNumberOfAssets: (value: number) => void;
  setNumberOfAliases: (value: number) => void;
  setNumberOfSubscriptions: (value: number) => void;
  setNumberOfSubscriptionPayments: (value: number) => void;
  setBurnedFunds: (value: number) => void;
  setCirculatingFunds: (value: number) => void;
  setThirdPartyApps: (value: ThirdPartyAppStorage) => void;
  setIsXTWalletDetected: (value: boolean) => void;
};
