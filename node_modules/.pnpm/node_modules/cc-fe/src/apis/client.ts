import axios from 'axios';

// 이전에 .env 파일에 설정했던 환경 변수를 가져옵니다.
const baseURL = import.meta.env.VITE_API_BASE_URL;

const client = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 앞으로 여기에 인터셉터 등을 추가하여 공통 로직을 처리할 수 있습니다.
// 예: client.interceptors.request.use(...)

export default client;
