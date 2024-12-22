import { useMemo } from "react";
import { Animator } from "@arwes/react";
import { FaCircleInfo } from "react-icons/fa6";
import { Puffs } from "@arwes/react-bgs";
import { useNodeConstants } from "@/hooks/useNodeConstants";
import * as classes from "./Alerts.css";

type variants = "warning" | "error" | "primary";

export const Alerts = () => {
  const { isTestnet } = useNodeConstants();

  const isUnsafeWebsite = useMemo(() => {
    const webUrl = new URL(window.location.href);
    return !!(
      webUrl.protocol !== "https:" &&
      webUrl.hostname !== "localhost" &&
      webUrl.hostname !== "127.0.0.1"
    );
  }, []);

  let title = "";
  let variant: variants = "primary";

  if (isUnsafeWebsite) {
    variant = "error";
    title =
      "🔒 Internet security is a necessity when it comes to personal information. This node must be secured with HTTPS 🔒";
  }

  if (isTestnet) {
    variant = "primary";
    title = "Hey young pandawan, this is a TESTNET node 🤓 🌎 🤟";
  }

  if (!title) return null;

  return (
    <section className={classes.container}>
      <div className={classes.alert({ variant })}>
        <div className={classes.content}>
          <FaCircleInfo />
          {title}
        </div>

        <div className={classes.animator}>
          <Animator active duration={{ interval: 1 }}>
            <Puffs color="hsla(0, 0%, 100%, 0.3)" quantity={50} />
          </Animator>
        </div>
      </div>
    </section>
  );
};
