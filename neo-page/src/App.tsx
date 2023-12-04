import { Routes, Route } from "react-router-dom";
import { Layout } from "./components/Layout";
import { NoMatchPage } from "./pages/noMatch";
import { HomePage } from "./pages/home";
import { ThirdPartyAppsPage } from "./pages/thirdParty";

import "@fontsource/titillium-web/400.css";
import "@fontsource/titillium-web/600.css";
import "@fontsource/titillium-web/700.css";
import "@fontsource/titillium-web/900.css";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<HomePage />} />
        <Route path="third-party" element={<ThirdPartyAppsPage />} />
        <Route path="*" element={<NoMatchPage />} />
      </Route>
    </Routes>
  );
}
