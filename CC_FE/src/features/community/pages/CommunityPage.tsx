import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { listBoards, type BoardSummary } from '@/apis/boards.api';

export default function CommunityPage() {
  const { data, isLoading, isError } = useQuery<BoardSummary[]>({
    queryKey: ['boards'],
    queryFn: () => listBoards(),
  });

  if (isLoading) return <div className="p-6">게시판 목록을 불러오는 중…</div>;
  if (isError || !data) return <div className="p-6">게시판 목록을 불러오는 데 실패했습니다.</div>;

  return (
    <div className="max-w-5xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-6">커뮤니티</h1>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {data.map((b) => (
          <article key={b.id} className="bg-white shadow rounded-md p-4 flex flex-col gap-2">
            <div className="flex items-center gap-3">
              {b.imageUrl ? (
                <img src={b.imageUrl} alt={b.name} className="w-12 h-12 rounded object-cover" />
              ) : (
                <div className="w-12 h-12 rounded bg-gray-200" />
              )}
              <div>
                <h2 className="text-lg font-semibold">
                  <Link to={`/boards?categoryId=${b.id}`} className="hover:underline">
                    {b.name}
                  </Link>
                </h2>
                {b.description && <p className="text-sm text-gray-600">{b.description}</p>}
              </div>
            </div>
            <div className="text-sm text-gray-700 mt-2">
              <div>
                총 게시글 수: <b>{b.totalPosts}</b>
              </div>
              <div>
                오늘 새 글: <b>{b.todayPosts}</b>
              </div>
              <div className="truncate">최근 글: {b.latestTitle ?? '-'}</div>
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}
