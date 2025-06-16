import {defineConfig, coverageConfigDefaults} from 'vitest/config'
import path from 'path';


export default defineConfig({
    test: {
        environment: 'jsdom',
        coverage: {
            provider: 'v8', // or 'v8'
            thresholds: {
                branches: 80,
            },
            exclude: ['**/main.jsx', ...coverageConfigDefaults.exclude],

        },
        globals: true,
        setupFiles: 'test/setup.tsx', // Pfad zur Setup-Datei
    },
    resolve: {
        alias: {
        '@': path.resolve(__dirname, './src'), // Use path.resolve here!
        },
    },
});