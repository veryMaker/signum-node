import { useMemo } from "react";
import { useThirdPartyApps } from "@/hooks/useThirdPartyApps";
import { useNodeConstants } from "@/hooks/useNodeConstants";
import {
  defaultContainer,
  defaultCardContainer,
} from "@/styles/containers.css";
import { NeoChip } from "@/components/NeoChip";
import { AppCard } from "./components/AppCard";
import { LoadingIndicator } from "./components/LoadingIndicator";
import { ViewSourceButton } from "./components/ViewSourceButton";
import * as classes from "./thirdParty.css";

export const ThirdPartyAppsPage = () => {
  const { isTestnet, isLoading: isLoadingNodeConstants } = useNodeConstants();
  const { thirdPartyApps, isLoading: isLoadingThirdPartyApps } =
    useThirdPartyApps();

  const isLoading = isLoadingNodeConstants || isLoadingThirdPartyApps;

  const apps = useMemo(() => {
    if (isLoading) return [];

    return isTestnet ? thirdPartyApps.testnet : thirdPartyApps.mainnet;
  }, [isLoading, thirdPartyApps, isTestnet]);

  return (
    <section className={defaultContainer}>
      <div className={classes.titleContainer}>
        <NeoChip label="Third Party Apps" />

        <ViewSourceButton />
      </div>

      {isLoading && (
        <div className={classes.loadingContainer}>
          <LoadingIndicator />
        </div>
      )}

      {!isLoading && (
        <div
          className={defaultCardContainer}
          style={{ justifyContent: "flex-start" }}
        >
          {apps.map((app) => (
            <AppCard key={app.title} {...app} />
          ))}
        </div>
      )}
    </section>
  );
};
