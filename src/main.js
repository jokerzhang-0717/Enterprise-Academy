import { createApp } from 'vue';
import App from './App.vue';
import router from './router/index.js';
import './index.css';

const app = createApp(App);
app.use(router);

// 等首个路由解析完成(含守卫重定向)后再挂载,避免地址栏与视图闪烁
router.isReady().then(() => {
  app.mount('#app');
});
