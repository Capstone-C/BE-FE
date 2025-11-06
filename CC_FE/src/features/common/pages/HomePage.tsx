import { Link } from 'react-router-dom';

export default function HomePage() {
  return (
    <main className="p-8">
      <h1 className="text-3xl font-bold underline text-blue-600">홈 페이지</h1>
      <div className="mt-4 flex gap-3">
        <Link to="/login" className="text-blue-600 hover:underline">
          로그인 페이지로 이동
        </Link>
        <Link to="/boards" className="rounded-md border px-4 py-2 hover:bg-gray-50">
          게시글 페이지로 이동
        </Link>
      </div>
    </main>
  );
}
