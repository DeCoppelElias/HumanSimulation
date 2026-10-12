import react from "@vitejs/plugin-react";
import { defineConfig } from "vitest/config";

export default defineConfig({
    plugins: [react()],
    // PixiJS is most of the bundle, so the limit sits just above it to catch growth beyond that.
    build: { outDir: "../target/classes/public", emptyOutDir: true, chunkSizeWarningLimit: 600 },
    server: { proxy: { "/worlds": "http://127.0.0.1:7070" } },
    test: { environment: "node", include: ["src/**/*.test.ts"] },
});
