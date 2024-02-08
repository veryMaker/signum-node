import { Outlet } from "react-router-dom";
import { theme } from "@/types";
import { Providers } from "@/providers";
import { Background } from "./components/Background";
import { Header } from "./components/Header";
import { Footer } from "./components/Footer";
import { Alerts } from "./components/Alerts";
import { SyncAlert } from "./components/SyncAlert";

export const Layout = () => {
  return (
    <Providers>
      <main style={{ position: "relative", overflow: "hidden" }}>
        <Background theme={theme} />

        <div style={{ position: "relative" }}>
          <SyncAlert />
          <Alerts />
          <Header />
          <Outlet />
        </div>

        <Footer />
      </main>
    </Providers>
  );
};
