import "@fontsource/titillium-web/400.css";
import "@fontsource/titillium-web/600.css";
import "@fontsource/titillium-web/700.css";
import "@fontsource/titillium-web/900.css";
import { ThemeProvider } from "./providers/ThemeProvider";
import { HomePage } from "./home";

export default function App() {
  return (
    <ThemeProvider>
      <HomePage />
    </ThemeProvider>
  );
}
