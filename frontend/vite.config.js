import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import viteSass from 'vite-plugin-sass'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(),viteSass()],
  server: {
    port: 3000,
    hot: true
  },
})