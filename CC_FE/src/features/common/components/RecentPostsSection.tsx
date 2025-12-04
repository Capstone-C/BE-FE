import React from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { listBoards, type BoardSummary } from '@/apis/boards.api';
import { usePosts } from '@/features/boards/hooks/usePosts';

// 각 게시판별 최신 글을 보여주는 컴포넌트
const BoardCard: React.FC<{ boardName: string; boardId: number }> = ({ boardName, boardId }) => {
  // 각 게시판(boardId)의 최신 글 3개를 조회
  const { data, isLoading } = usePosts({
    boardId,
    size: 3,
    sort: 'createdAt', // 최신순
  });

  const posts = data?.content ?? [];

  return (
    <div className="bg-white rounded-lg shadow-lg p-6 flex flex-col hover:shadow-xl transition-shadow duration-300 border border-gray-100">
      <h3 className="text-xl font-bold text-gray-800 pb-3 mb-4 border-b-2 border-[#71853A]">
        {boardName}
      </h3>
      <div className="flex-grow">
        {isLoading ? (
          <p className="text-gray-500 text-sm text-center py-4">글 목록을 불러오는 중...</p>
        ) : posts.length > 0 ? (
          <ul className="space-y-3">
            {posts.map((post) => (
              <li key={post.id} className="truncate">
                <Link
                  to={`/boards/${post.id}`}
                  className="text-gray-600 hover:text-[#4E652F] hover:underline block truncate text-sm"
                >
                  {post.title}
                </Link>
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-gray-400 text-sm text-center py-4">등록된 글이 없습니다.</p>
        )}
      </div>
      <div className="mt-6 text-right">
        <Link
          to={`/boards?categoryId=${boardId}`}
          className="text-sm font-semibold text-[#4E652F] hover:text-[#425528]"
        >
          더보기 &rarr;
        </Link>
      </div>
    </div>
  );
};

const RecentPostsSection: React.FC = () => {
  // 전체 게시판 목록 조회
  const { data: boards } = useQuery<BoardSummary[]>({
    queryKey: ['boards'],
    queryFn: () => listBoards(),
  });

  // 레시피 게시판을 제외하고 싶다면 여기서 필터링할 수 있으나,
  // API에서 type 정보를 주지 않는다면 이름 등으로 구분해야 합니다.
  // 현재는 모든 게시판을 최대 4개까지 보여줍니다.
  const displayBoards = boards?.slice(0, 4) ?? [];

  return (
    <section className="py-16 bg-[#F0F5E5]">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <h2 className="text-2xl font-bold text-gray-900 mb-8">커뮤니티 최신 글</h2>
        {displayBoards.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
            {displayBoards.map((board) => (
              <BoardCard
                key={board.id}
                boardId={board.id}
                boardName={board.name}
              />
            ))}
          </div>
        ) : (
          <div className="text-center text-gray-500 py-10">
            게시판 정보를 불러오는 중이거나 게시판이 없습니다.
          </div>
        )}
      </div>
    </section>
  );
};

export default RecentPostsSection;