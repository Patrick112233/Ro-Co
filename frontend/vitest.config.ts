import {defineConfig, coverageConfigDefaults} from 'vitest/config'

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
            '@': '/src', // Alias für das src-Verzeichnis   
        },
    },
});