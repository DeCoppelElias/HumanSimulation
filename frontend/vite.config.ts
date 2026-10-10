import react from "@vitejs/plugin-react";
import { defineConfig } from "vitest/config";

export default defineConfig({
    plugins: [react()],
    build: { outDir: "../target/classes/public", emptyOutDir: true },
    server: { proxy: { "/worlds": "http://127.0.0.1:7070" } },
    test: { environment: "node", include: ["src/**/*.test.ts"] },
});
