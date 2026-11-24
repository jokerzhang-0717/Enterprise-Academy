import { createApp } from 'vue';
import App from './App.vue';
import router from './router/index.js';
import revealDirective, { revealRowsDirective } from './composables/scrollReveal.js';
import countUpDirective from './composables/countUp.js';
import './index.css';

const app = createApp(App);
app.use(router);
app.directive('reveal', revealDirective);
app.directive('reveal-rows', revealRowsDirective);
app.directive('count-up', countUpDirective);

// 等首个路由解析完成(含守卫重定向)后再挂载,避免地址栏与视图闪烁
router.isReady().then(() => {
  app.mount('#app');
});
