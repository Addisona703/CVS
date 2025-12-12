import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import { resolve } from 'path'

export default defineConfig({
  plugins: [
    uni({
      vueOptions: {
        reactivityTransform: false
      }
    })
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, '.')
    }
  },
  server: {
    port: 3001,
    host: '0.0.0.0',
    open: false
  },
  build: {
    target: 'es2015',
    cssTarget: 'chrome61',
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    }
  },
  ssr: {
    noExternal: ['echarts', 'uview-plus']
  },
  optimizeDeps: {
    exclude: ['uview-plus']
  }
})
