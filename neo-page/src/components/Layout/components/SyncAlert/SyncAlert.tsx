import { FrameSVGUnderline } from "@arwes/react-frames";
import { SyncChip } from "@/components/SyncChip";
import * as classes from "./SyncAlert.css";

export const SyncAlert = () => {
  return (
    <section className={classes.container}>
      <SyncChip />
      <FrameSVGUnderline className={classes.frame} />
    </section>
  );
};
