import axios from 'axios';

// const baseURL = import.meta.env.VITE_API_BASE_URL;
const baseURL = '/';

// 1. 인증이 필요 없는 '공개' API용 클라이언트
export const publicClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 2. 인증 토큰이 필요한 API용 클라이언트
export const authClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// authClient에만 인터셉터를 설정하여 토큰을 추가하는 로직 (미래를 위한 준비)
authClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken'); // 예시: 토큰을 로컬 스토리지에서 가져옴
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);
export default class client {
}