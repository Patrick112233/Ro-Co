import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
//import viteSass from 'vite-plugin-sass'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api/v1': {
        target: 'https://localhost:443',
        changeOrigin: true,
        secure: false,
      },
    },
  },
})