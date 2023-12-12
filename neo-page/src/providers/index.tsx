import { Fragment } from "react";
import type { Props } from "./types";
import { ThemeProvider } from "./ThemeProvider";
import { WalletProvider } from "./WalletProvider";

// Additional Providers will be added here
export const Providers = ({ children }: Props) => {
  return (
    <Fragment>
      <ThemeProvider>
        <WalletProvider>{children}</WalletProvider>
      </ThemeProvider>
    </Fragment>
  );
};
