import { NeoChip } from "@/components/NeoChip";
import {
  defaultContainer,
  defaultCardContainer,
} from "@/styles/containers.css";
import { VideoTutorialCard } from "./components/VideoTutorialCard";

export const Videotutorials = () => {
  return (
    <section className={defaultContainer}>
      <div style={{ display: "flex" }}>
        <NeoChip label="Videotutorials" />
      </div>

      <div className={defaultCardContainer}>
        <VideoTutorialCard
          title="Setup Signum Account"
          href="https://www.youtube.com/watch?v=seUnm0GEwXY"
        />

        <VideoTutorialCard
          title="Learn to Mine Signa"
          href="https://www.youtube.com/watch?v=zeIVCKN6Kpo"
        />

        <VideoTutorialCard
          title="Manage your Signa commitment"
          href="https://www.youtube.com/watch?v=p-jEkv3aGAs"
        />
      </div>
    </section>
  );
};
