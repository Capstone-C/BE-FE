import { useQuery } from '@tanstack/react-query';
import { getTestData } from '@/apis/test'; // 방금 만든 API 함수를 import

function App() {
  // useQuery 훅 사용
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['testData'], // 이 쿼리의 고유한 키 (배열로 감싸기)
    queryFn: getTestData, // 데이터를 가져올 함수
  });

  // 로딩 중일 때 보여줄 UI
  if (isLoading) {
    return <div>로딩 중입니다...</div>;
  }

  // 에러가 발생했을 때 보여줄 UI
  if (isError) {
    return <div>에러가 발생했습니다: {error.message}</div>;
  }

  return (
    <>
      <h1>Vite + React</h1>
      <p>TanStack Query를 사용한 API 데이터:</p>
      {/* 데이터를 JSON 문자열로 예쁘게 보여주기 */}
      <pre>{JSON.stringify(data, null, 2)}</pre>
    </>
  );
}

export default App;

// import { useEffect } from 'react';
// import client from '@/apis/client';

// function App() {
//   useEffect(() => {
//     const testApiCall = async () => {
//       try {
//         // 백엔드에 '/test' 라는 엔드포인트가 있다고 가정
//         const response = await client.get('/test');
//         console.log('API 응답:', response.data);
//       } catch (error) {
//         console.error('API 호출 에러:', error);
//       }
//     };

//     testApiCall();
//   }, []);

//   return (
//     <>
//       <h1>Vite + React</h1>
//       <p>F12를 눌러 콘솔에서 API 호출 결과를 확인하세요.</p>
//     </>
//   );
// }

// export default App;

// import { useState } from 'react';
// import reactLogo from './assets/react.svg';
// import viteLogo from '/vite.svg';
// import './App.css';

// function App() {
//   const [count, setCount] = useState(0);
//   console.log('API Base URL:', import.meta.env.VITE_API_BASE_URL);

//   return (
//     <>
//       <div>
//         <a href="https://vite.dev" target="_blank">
//           <img src={viteLogo} className="logo" alt="Vite logo" />
//         </a>
//         <a href="https://react.dev" target="_blank">
//           <img src={reactLogo} className="logo react" alt="React logo" />
//         </a>
//       </div>
//       <h1>Vite + React</h1>
//       <div className="card">
//         <button onClick={() => setCount((count) => count + 1)}>count is {count}</button>
//         <p>
//           Edit <code>src/App.tsx</code> and save to test HMR
//         </p>
//       </div>
//       <p className="read-the-docs">Click on the Vite and React logos to learn more</p>
//     </>
//   );
// }

// export default App;
