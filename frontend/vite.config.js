import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'; // Importiere das path-Modul

// https://vite.dev/config/
export default defineConfig({
  base: "/",
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'), // Alias für das src-Verzeichnis
    },
  },
  server: {
    port: 443,
    proxy: {
      '/api/v1': {
        target:  process.env.BACKEND_URL || 'https://loclahost:443', //set API target to backend
        changeOrigin: true,
        secure: false, //deactivate SSL certificate validation
      },
    },
  },
})