import { useCallback, useEffect } from "react";
import { useStore } from "@/states";
import {
  ExtensionWalletError,
  GenericExtensionWallet,
} from "@signumjs/wallets";
import type { Props } from "./types";

const config = {
  name: "Signum Node Website",
  networkName: "Signum",
  wallet: {
    Extension: new GenericExtensionWallet(),
  },
};

// This provider is only used to detect if the user has installed the Signum XT Wallet
export function WalletProvider({ children }: Props) {
  const setIsXTWalletDetected = useStore(
    (state) => state.setIsXTWalletDetected
  );

  const handleConnectWallet = useCallback(async () => {
    try {
      await config.wallet.Extension.connect({
        appName: config.name,
        networkName: config.networkName,
      });

      setIsXTWalletDetected(true);
    } catch (e) {
      // We only listen for wallet installed feedback, not the others
      if (e instanceof ExtensionWalletError) {
        if (e.name === "NotFoundWalletError") setIsXTWalletDetected(false);
      }
    }
  }, [setIsXTWalletDetected]);

  useEffect(() => {
    handleConnectWallet();
  }, [handleConnectWallet]);

  return children;
}
