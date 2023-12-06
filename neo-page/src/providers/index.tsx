import { Fragment } from "react";
import type { Props } from "./types";
import { ThemeProvider } from "./ThemeProvider";

// Additional Providers will be added here
export const Providers = ({ children }: Props) => {
  return (
    <Fragment>
      <ThemeProvider>{children}</ThemeProvider>
    </Fragment>
  );
};
