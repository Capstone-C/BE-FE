import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { useAuthorComments } from '@/features/comments/hooks/useAuthorComments';
import { formatYMDHMKorean } from '@/utils/date';

export default function MyCommentsPage() {
  const { user } = useAuth();
  const location = useLocation();

  const sp = new URLSearchParams(location.search);
  const authorIdParam = sp.get('authorId');
  const authorId = authorIdParam ? Number(authorIdParam) : user?.id;

  const { data, isLoading, isError } = useAuthorComments(authorId);

  if (!authorId) {
    return <div className="max-w-7xl mx-auto p-8 text-center text-red-600">작성자 정보를 확인할 수 없습니다.</div>;
  }

  if (isLoading) return <div className="max-w-7xl mx-auto p-8 text-center">댓글을 불러오는 중…</div>;
  if (isError || !data) return <div className="max-w-7xl mx-auto p-8 text-center text-red-600">댓글을 불러오지 못했습니다.</div>;

  const isMyComments = authorId === user?.id;
  const pageTitle = isMyComments ? '내가 쓴 댓글' : '작성자 댓글';
  const pageDescription = isMyComments
    ? `${user?.nickname}님이 작성하신 댓글 목록입니다.`
    : '해당 회원이 작성한 댓글 목록입니다.';

  return (
    <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
      <div className="flex flex-col md:flex-row gap-8">
        {/* Main Content */}
        <div className="flex-1 min-w-0">
          <div className="mb-6 pb-4 border-b border-gray-200">
            <h1 className="text-3xl font-bold leading-tight text-gray-900">
              {pageTitle}
            </h1>
            <p className="mt-2 text-md text-gray-500">
              {pageDescription}
            </p>
          </div>

          <div className="bg-white rounded-lg shadow-md overflow-hidden">
            {data.length > 0 ? (
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                  <tr>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-1/2">
                      댓글 내용
                    </th>
                    <th scope="col" className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider w-1/3">
                      원본 게시글
                    </th>
                    <th scope="col" className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider w-32">
                      날짜
                    </th>
                  </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                  {data.map((c) => (
                    <tr key={c.commentId} className="hover:bg-gray-50 transition-colors">
                      <td className="px-6 py-4 text-sm text-gray-900">
                        <div className="line-clamp-2 break-all">
                          {c.content}
                        </div>
                      </td>
                      <td className="px-6 py-4 text-sm text-center text-gray-500">
                        <Link
                          to={`/boards/${c.postId}`}
                          className="text-[#4E652F] hover:text-[#425528] hover:underline block truncate max-w-[200px] mx-auto font-medium"
                          title={c.postTitle}
                        >
                          {c.postTitle}
                        </Link>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-center text-gray-500 text-xs">
                        {formatYMDHMKorean(c.createdAt)}
                      </td>
                    </tr>
                  ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="text-center py-20 text-gray-500 bg-gray-50">
                작성한 댓글이 없습니다.
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}