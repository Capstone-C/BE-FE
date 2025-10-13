import client from './client';

// '/test' 엔드포인트에서 데이터를 가져오는 함수
export const getTestData = async () => {
  const response = await client.get('/test');
  return response.data; // 실제 데이터만 반환
};
