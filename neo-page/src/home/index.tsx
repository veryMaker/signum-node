import { Fragment } from "react";
import { useStore } from "@/states/store";
import { Header } from "@/components/Header";

export function HomePage() {
  const count = useStore((state) => state.count);
  const firstName = useStore((state) => state.firstName);
  const setFirstName = useStore((state) => state.setFirstName);
  const increment = useStore((state) => state.increment);
  const decrement = useStore((state) => state.decrement);

  const incrementByTwo = () => increment(2);
  const decreaseByTwo = () => decrement(2);
  const setRandomName = () => setFirstName("ipr");

  return (
    <Fragment>
      <Header />

      <p>First Name is:{firstName}</p>
      <p>Count is:{count}</p>

      <button onClick={setRandomName}>Set Random Name</button>
      <button onClick={incrementByTwo}>Increment</button>
      <button onClick={decreaseByTwo}>Increment</button>
    </Fragment>
  );
}
