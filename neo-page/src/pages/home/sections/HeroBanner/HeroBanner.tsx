import { Fragment } from "react";
import { useStore } from "@/states";
import { useNodeConstants } from "@/hooks/useNodeConstants";
import { Alert } from "./components/Alert";

export const HeroBanner = () => {
  const { isDefaultCashbackIdSet } = useNodeConstants();
  const isXTWalletDetected = useStore((state) => state.isXTWalletDetected);

  const canShowXTWalletAlert = !isDefaultCashbackIdSet && !isXTWalletDetected;

  return (
    <Fragment>
      <Alert
        // If default cashback is set, this alert will also be show
        show={isDefaultCashbackIdSet}
        title="Get 25% Cashback from your node fees"
        href="https://docs.signum.network/signum/activate-cashback"
        cta="Learn More"
      />

      <Alert
        // Show XT Wallet Alert Only When the user does not have installed XT Wallet
        // If default cashback is set, this alert will also be hidden
        show={canShowXTWalletAlert}
        title="Install XT Wallet"
        href="https://docs.signum.network/ecosystem/wallets#jjLLe"
        cta="Learn More"
      />
    </Fragment>
  );
};
