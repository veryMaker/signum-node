import { defineConfig } from "vite";
import { vanillaExtractPlugin } from "@vanilla-extract/vite-plugin";
import react from "@vitejs/plugin-react-swc";
import path from "path";

// Learn about Vite config: https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    react(),
    // Learn about Vanilla Extract Plugin: https://vanilla-extract.style/documentation/integrations/vite/
    vanillaExtractPlugin({
      identifiers: process.env.NODE_ENV === "development" ? "debug" : "short",
    }),
  ],
  resolve: {
    alias: { "@": path.resolve(__dirname, "./src") },
  },
});
