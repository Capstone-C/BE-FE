// src/apis/client.ts (올바른 최종본)
import axios, { AxiosError } from 'axios';
import { getToken, removeToken } from '@/utils/token';

// publicClient는 아무런 인터셉터가 없어야 합니다.
const publicClient = axios.create({
  baseURL: '/',
});

const authClient = axios.create({
  baseURL: '/',
});

// 요청 인터셉터는 authClient에만 적용되어야 합니다.
authClient.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// 응답 인터셉터도 authClient에만 적용되어야 합니다.
authClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      removeToken();
      alert('세션이 만료되었습니다. 다시 로그인해주세요.');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  },
);

export { publicClient, authClient };
