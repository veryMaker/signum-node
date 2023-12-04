import { Routes, Route } from "react-router-dom";
import { ThemeProvider } from "./providers/ThemeProvider";
import { HomePage } from "./pages/home";
import { NoMatchPage } from "./pages/noMatch";

import "@fontsource/titillium-web/400.css";
import "@fontsource/titillium-web/600.css";
import "@fontsource/titillium-web/700.css";
import "@fontsource/titillium-web/900.css";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<ThemeProvider />}>
        <Route index element={<HomePage />} />
        <Route path="*" element={<NoMatchPage />} />
      </Route>
    </Routes>
  );
}
