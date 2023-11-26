import {
  type AppTheme,
  GridLines,
  Dots,
  MovingLines,
  Animator,
} from "@arwes/react";

interface Props {
  theme: AppTheme;
}

export const Background = ({ theme }: Props) => {
  return (
    <Animator merge combine>
      <div
        style={{
          position: "absolute",
          inset: 0,
          background: `linear-gradient(rgba(0,0,0,0.9), rgba(0,0,0,0.9)), url(images/bg.webp)`,
          backgroundPosition: "center",
          backgroundSize: "cover",
          backgroundRepeat: "no-repeat",
          minHeight: "100vh",
        }}
      >
        <GridLines lineColor={theme.colors.primary.deco(0)} />
        <Dots color={theme.colors.primary.deco(2)} />
        <MovingLines lineColor={theme.colors.primary.deco(8)} />
      </div>
    </Animator>
  );
};
