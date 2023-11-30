import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";
import { vanillaExtractPlugin } from '@vanilla-extract/vite-plugin';
import path from "path";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react({
    jsxImportSource: "@emotion/react",
  }), vanillaExtractPlugin({
    identifiers: process.env.NODE_ENV === 'development' ? 'debug' : 'short',
  })],
  resolve: {
    alias: { "@": path.resolve(__dirname, "./src") },
  },
});
