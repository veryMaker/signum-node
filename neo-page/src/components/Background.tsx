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
  const color = theme.colors.primary.deco(1)
  return (
    <Animator duration={{ interval: 10 }}>
      <div style={{
        position: 'absolute',
        left: 0,
        right: 0,
        top: 0,
        bottom: 0,

        // backgroundImage: 'radial-gradient(85% 85% at 50% 50%, hsla(185, 100%, 25%, 0.25) 0%, hsla(185, 100%, 25%, 0.12) 50%, hsla(185, 100%, 25%, 0) 100%)'
      }}>
        <GridLines
          lineColor={color}
          distance={50}
        />
        <Dots
          color={color}
          distance={50}
        />
        <MovingLines
          lineColor={color}
          distance={50}
          sets={20}
        />
      </div>
    </Animator>
  );
};
