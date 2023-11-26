export type State = {
  firstName: string;
  lastName: string;
  count: number;
};

export type Action = {
  setFirstName: (value: State["firstName"]) => void;
  setLastName: (value: State["lastName"]) => void;
  increment: (qty: number) => void;
  decrement: (qty: number) => void;
};
