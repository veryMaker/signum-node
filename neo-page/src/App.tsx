import { Routes, Route } from "react-router-dom";
import { Layout } from "./components/Layout";
import { NoMatchPage } from "./pages/noMatch";
import { HomePage } from "./pages/home";
import { AppsPage } from "./pages/apps";
import { StatsPage } from "./pages/stats";
import { DocsPage } from "./pages/docs";

import "@fontsource/titillium-web/400.css";
import "@fontsource/titillium-web/600.css";
import "@fontsource/titillium-web/700.css";
import "@fontsource/titillium-web/900.css";
import "react-tooltip/dist/react-tooltip.css";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<HomePage />} />
        <Route path="apps" element={<AppsPage />} />
        <Route path="stats" element={<StatsPage />} />
        <Route path="docs" element={<DocsPage />} />
        <Route path="*" element={<NoMatchPage />} />
      </Route>
    </Routes>
  );
}
