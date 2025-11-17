import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { useAuthorComments } from '@/features/comments/hooks/useAuthorComments';

export default function MyCommentsPage() {
  const { user } = useAuth();
  const location = useLocation();

  const sp = new URLSearchParams(location.search);
  const authorIdParam = sp.get('authorId');
  const authorId = authorIdParam ? Number(authorIdParam) : user?.id;

  const { data, isLoading, isError } = useAuthorComments(authorId);

  if (!authorId) {
    return <div className="p-6">작성자 정보를 확인할 수 없습니다.</div>;
  }

  if (isLoading) return <div className="p-6">댓글을 불러오는 중…</div>;
  if (isError || !data) return <div className="p-6">댓글을 불러오지 못했습니다.</div>;

  return (
    <div className="max-w-3xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-4">{authorId === user?.id ? '내가 쓴 댓글' : '작성자 댓글'}</h1>
      {data.length === 0 ? (
        <div className="text-gray-600">작성한 댓글이 없습니다.</div>
      ) : (
        <ul className="divide-y">
          {data.map((c) => (
            <li key={c.commentId} className="py-3">
              <div className="text-sm text-gray-500">{new Date(c.createdAt).toLocaleString()}</div>
              <div className="text-gray-900">{c.content}</div>
              <Link to={`/boards/${c.postId}`} className="text-blue-600 text-sm hover:underline">
                게시글: {c.postTitle}
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
