import { http } from './client.js';

export const completeOnboarding = () => http.post('/auth/onboarding/complete');
