import { useEffect } from 'react';
import client from '@/apis/client';

function App() {
  useEffect(() => {
    const testApiCall = async () => {
      try {
        // 백엔드에 '/test' 라는 엔드포인트가 있다고 가정
        const response = await client.get('/test');
        console.log('API 응답:', response.data);
      } catch (error) {
        console.error('API 호출 에러:', error);
      }
    };

    testApiCall();
  }, []);

  return (
    <>
      <h1>Vite + React</h1>
      <p>F12를 눌러 콘솔에서 API 호출 결과를 확인하세요.</p>
    </>
  );
}

export default App;

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
