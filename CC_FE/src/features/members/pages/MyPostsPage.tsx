import { Link, useSearchParams } from 'react-router-dom';
import { usePosts } from '../../boards/hooks/usePosts';
import { formatYMDHMKorean } from '../../../utils/date';
import { extractAuthorRef } from '../../../utils/author';
import { useBlockedMembers } from '../hooks/useMemberBlocks';
import { useAuth } from '../../../hooks/useAuth'; // useAuth 추가

// 이 페이지는 오직 현재 로그인된 사용자의 글만 보여줍니다.
export default function MyPostsPage() {
  const { user } = useAuth(); // 현재 로그인된 사용자 정보
  const [sp, setSp] = useSearchParams();

  const page = Number(sp.get('page') ?? 1);
  const size = Number(sp.get('size') ?? 20);
  const boardId = sp.get('categoryId');
  const searchType = sp.get('searchType') ?? undefined;
  const keyword = sp.get('keyword') ?? undefined;

  const currentAuthorId = user?.id; // 현재 로그인된 사용자 ID를 authorId로 사용

  const { data, isLoading, isError } = usePosts({
    page,
    size,
    keyword,
    sort: 'createdAt',
    boardId: boardId ? Number(boardId) : undefined,
    searchType,
    authorId: currentAuthorId, // <<-- 이 부분이 핵심: 현재 사용자 ID로 필터링
  });

  const { data: blocked } = useBlockedMembers();
  const blockedIds = blocked?.map((b) => b.blockedId) ?? [];

  if (!user) return <div className="max-w-7xl mx-auto p-8 text-center text-red-600">로그인이 필요합니다.</div>;
  if (isLoading) return <div className="max-w-7xl mx-auto p-8 text-center">내가 쓴 글 목록 불러오는 중…</div>;
  if (isError || !data) return <div className="max-w-7xl mx-auto p-8 text-center text-red-600">오류가 발생했습니다.</div>;

  const visiblePosts = data.content.filter((p) => {
    const { memberId } = extractAuthorRef(p as any);
    return !(memberId && blockedIds.includes(memberId));
  });

  // 페이지네이션 핸들러
  const handlePageChange = (newPage: number) => {
    const newSp = new URLSearchParams(sp);
    newSp.set('page', String(newPage));
    setSp(newSp);
  };

  return (
    <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
      <div className="flex flex-col md:flex-row gap-8">
        {/* Main Content */}
        <div className="flex-1 min-w-0">
          <div className="mb-6 pb-4 border-b border-gray-200 flex justify-between items-end">
            <div>
              <h1 className="text-3xl font-bold leading-tight text-gray-900">
                내가 쓴 글
              </h1>
              <p className="mt-2 text-md text-gray-500">
                {user.nickname}님이 작성하신 게시글 목록입니다.
              </p>
            </div>
            <Link
              to="/boards/new"
              className="px-4 py-2 bg-[#4E652F] text-white text-sm font-medium rounded-md hover:bg-[#425528] transition-colors flex-shrink-0"
            >
              글쓰기
            </Link>
          </div>

          <div className="bg-white rounded-lg shadow-md overflow-hidden">
            {visiblePosts.length > 0 ? (
              <>
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                    <tr>
                      <th scope="col" className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider w-16">번호</th>
                      <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">제목</th>
                      <th scope="col" className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider w-32">카테고리</th>
                      <th scope="col" className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider w-20">추천</th>
                      <th scope="col" className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider w-20">조회</th>
                      <th scope="col" className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider w-32">날짜</th>
                    </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                    {visiblePosts.map((post) => {
                      return (
                        <tr key={post.id} className="hover:bg-gray-50 transition-colors">
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-center text-gray-500">{post.id}</td>
                          <td className="px-6 py-4 text-sm text-gray-900">
                            <Link to={`/boards/${post.id}`} className="block hover:text-[#4E652F] font-medium">
                              {post.title}
                              {post.commentCount > 0 && (
                                <span className="ml-2 text-xs font-semibold text-[#71853A]">
                                                        [{post.commentCount}]
                                                    </span>
                              )}
                            </Link>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-center text-gray-500">{post.categoryName}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-center text-gray-500">{post.likeCount}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-center text-gray-500">{post.viewCount}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-center text-gray-500 text-xs">
                            {formatYMDHMKorean(post.createdAt).split(' ')[0]} {/* 날짜만 표시 */}
                          </td>
                        </tr>
                      );
                    })}
                    </tbody>
                  </table>
                </div>

                {/* Pagination */}
                <div className="p-4 flex justify-center items-center space-x-2 border-t">
                  <button
                    onClick={() => handlePageChange(page - 1)}
                    disabled={page <= 1}
                    className="px-3 py-1 text-sm rounded-md border border-gray-300 bg-white hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    이전
                  </button>
                  <span className="text-sm text-gray-600 px-2">
                        {page} / {data.totalPages}
                    </span>
                  <button
                    onClick={() => handlePageChange(page + 1)}
                    disabled={page >= data.totalPages}
                    className="px-3 py-1 text-sm rounded-md border border-gray-300 bg-white hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    다음
                  </button>
                </div>
              </>
            ) : (
              <div className="text-center py-20 text-gray-500">
                작성하신 게시글이 없습니다.
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}