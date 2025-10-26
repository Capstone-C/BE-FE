import axios, { AxiosError } from 'axios'; // AxiosError를 axios에서 가져옵니다.
import { getToken, removeToken } from '@/utils/token'; // removeToken을 token 유틸리티에서 가져옵니다.

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
    return config;
  },
  (error) => Promise.reject(error),
);

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
