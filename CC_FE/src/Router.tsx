import { BrowserRouter, Route, Routes } from 'react-router-dom';
import HomePage from '@/pages/HomePage';
import LoginPage from '@/pages/LoginPage';

const Router = () => {
  return (
    <BrowserRouter>
      <Routes>
        {/* 주소가 '/' 이면 HomePage 컴포넌트를 보여줍니다. */}
        <Route path="/" element={<HomePage />} />

        {/* 주소가 '/login' 이면 LoginPage 컴포넌트를 보여줍니다. */}
        <Route path="/login" element={<LoginPage />} />
      </Routes>
    </BrowserRouter>
  );
};

export default Router;