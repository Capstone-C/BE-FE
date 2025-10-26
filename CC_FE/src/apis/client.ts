import axios from 'axios';
import { getToken } from '@/utils/token';

// baseURL을 명시하지 않거나 '/'로 설정합니다.
// 이렇게 해야 모든 요청이 현재 페이지의 origin(예: http://localhost:5173)을 기준으로 전송되며,
// Vite의 프록시 설정이 정상적으로 동작할 수 있습니다.
const publicClient = axios.create({
  baseURL: '/',
});

const authClient = axios.create({
  baseURL: '/',
});

authClient.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    // 요청 URL이 '/api'로 시작하는지 확인하는 것이 좋습니다.
    // 만약 public asset 등을 요청할 경우를 대비할 수 있습니다.
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

export { publicClient, authClient };