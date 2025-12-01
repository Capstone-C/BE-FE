import { Link, useSearchParams } from 'react-router-dom';
import { usePosts } from '../hooks/usePosts'; // 상대 경로 수정
import BoardSidebar from '../components/BoardSidebar'; // 상대 경로 수정
import { formatYMDHMKorean } from '../../../utils/date'; // 상대 경로 수정
import { extractAuthorRef, getDisplayName } from '../../../utils/author'; // 상대 경로 수정
import { useBlockedMembers } from '../../members/hooks/useMemberBlocks'; // 상대 경로 수정

export default function BoardsListPage() {
  const [sp, setSp] = useSearchParams();

  const page = Number(sp.get('page') ?? 1);
  const size = Number(sp.get('size') ?? 20);
  const boardId = sp.get('categoryId');
  // [추가] URL에서 authorId를 가져옵니다.
  const authorIdParam = sp.get('authorId');
  const authorId = authorIdParam ? Number(authorIdParam) : undefined;

  const searchType = sp.get('searchType') ?? undefined;
  const keyword = sp.get('keyword') ?? undefined;

  const { data, isLoading, isError } = usePosts({
    page,
    size,
    keyword,
    sort: 'createdAt',
    boardId: boardId ? Number(boardId) : undefined,
    searchType,
    authorId: authorId, // <<-- authorId를 쿼리에 전달
  });

  const { data: blocked } = useBlockedMembers();
  const blockedIds = blocked?.map((b) => b.blockedId) ?? [];

  if (isLoading) return <div className="max-w-7xl mx-auto p-8 text-center">목록 불러오는 중…</div>;
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

  const pageTitle = authorId ? `${authorId}번 회원이 쓴 글` : (boardId ? '게시판' : '전체 글');

  return (
    <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
      <div className="flex flex-col md:flex-row gap-8">

        {/* Sidebar */}
        <BoardSidebar />

        {/* Main Content */}
        <div className="flex-1 min-w-0">
          <div className="mb-6 pb-4 border-b border-gray-200 flex justify-between items-end">
            <div>
              <h1 className="text-3xl font-bold leading-tight text-gray-900">
                {pageTitle}
              </h1>
              <p className="mt-2 text-md text-gray-500">
                다양한 식단 정보를 공유하고 소통해보세요.
              </p>
            </div>
            <Link
              to="/boards/new"
              state={boardId ? { fromCategoryId: Number(boardId) } : undefined}
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
                      <th scope="col" className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider w-32">글쓴이</th>
                      <th scope="col" className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider w-20">추천</th>
                      <th scope="col" className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider w-20">조회</th>
                      <th scope="col" className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider w-32">날짜</th>
                    </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                    {visiblePosts.map((post) => {
                      const { inlineName, memberId: postAuthorId } = extractAuthorRef(post);
                      const authorName = getDisplayName(postAuthorId, inlineName);
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
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-center text-gray-500">{authorName}</td>
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
                게시글이 존재하지 않습니다.
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}