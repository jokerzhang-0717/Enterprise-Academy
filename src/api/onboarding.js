import { http } from './client.js';

/** 记录当前登录用户已完成新员工入职手册阅读。 */
export const completeOnboarding = () => http.post('/auth/onboarding/complete');
