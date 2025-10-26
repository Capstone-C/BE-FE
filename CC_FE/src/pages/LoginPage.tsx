import { Link } from 'react-router-dom';

export default function LoginPage() {
  return (
    <main>
      <h1>로그인</h1>
      <Link to="/">홈으로</Link>
      <div className="mt-4">
        계정이 없으신가요? <Link to="/signup" className="text-blue-600 hover:underline">회원가입</Link>
      </div>
    </main>
  );
}