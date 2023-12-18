import { useMemo } from "react";
import { localInterval, remoteInterval } from "@/types";

export const useFetchingInterval = () => {
  const interval = useMemo(() => {
    const webUrl = new URL(window.location.href);

    return webUrl.hostname === "localhost" || webUrl.hostname === "127.0.0.1"
      ? localInterval
      : remoteInterval;
  }, []);

  return interval;
};
