import { create } from "zustand";
import { devtools, persist } from "zustand/middleware";
import type { State, Action } from "./types";

// Zustand docs recommends to colocate actions and states within the store
export const useStore = create<State & Action>()(
  devtools(
    persist(
      (set) => ({
        firstName: "",
        lastName: "",
        count: 0,
        setFirstName: (value) => set(() => ({ firstName: value })),
        setLastName: (value) => set(() => ({ lastName: value })),
        increment: (qty) => set((state) => ({ count: state.count + qty })),
        decrement: (qty) => set((state) => ({ count: state.count - qty })),
      }),
      { name: "app-storage", version: 1 }
    )
  )
);
