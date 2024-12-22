export type ThirdPartyApp = {
  title: string;
  description: string;
  img: string;
  url: string;
};

export type ThirdPartyAppStorage = {
  mainnet: ThirdPartyApp[];
  testnet: ThirdPartyApp[];
};
