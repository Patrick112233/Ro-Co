import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  console.log('VITE_BACKEND_URL:', env.VITE_BACKEND_URL); // Debug print


  return {
    base: "/",
    plugins: [react()],
    define: {
      'process.env': {
        BACKEND_URL: env.VITE_BACKEND_URL
      },
    },
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    server: {
      port: 3000,
      /*proxy: {
        '/api/v1': {
          target: env.VITE_BACKEND_URL,
          changeOrigin: true,
          secure: false,
          configure: (proxy, options) => {
            proxy.on('proxyReq', (proxyReq, req, res) => {
              console.log(`[VITE PROXY] ${req.method} ${req.url} -> ${env.VITE_BACKEND_URL}${req.url}`);
            });
          },
        },
      },*/
    },
  }
});