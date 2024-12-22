export const formatNumber = (value: number) =>
  new Intl.NumberFormat("en-US").format(value);
