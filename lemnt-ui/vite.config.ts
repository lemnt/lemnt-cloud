import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@': new URL('./src', import.meta.url).pathname },
  },
  build: {
    rollupOptions: {
      output: {
        // Pro Components 内部存在循环引用，将第三方组件放入同一块保证执行顺序。
        manualChunks: (id) => (id.includes('/node_modules/') ? 'vendor' : undefined),
      },
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/dev-api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/dev-api/, ''),
      },
    },
  },
});
