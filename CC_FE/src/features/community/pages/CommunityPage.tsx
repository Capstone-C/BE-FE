import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { listBoards, type BoardSummary } from '@/apis/boards.api';
import Container from '@/components/ui/Container';
import { Card } from '@/components/ui/Card';

export default function CommunityPage() {
  const { data, isLoading, isError } = useQuery<BoardSummary[]>({
    queryKey: ['boards'],
    queryFn: () => listBoards(),
  });

  if (isLoading) return <div className="p-6">게시판 목록을 불러오는 중…</div>;
  if (isError || !data) return <div className="p-6">게시판 목록을 불러오는 데 실패했습니다.</div>;

  const hasBoards = data.length > 0;

  return (
    <Container className="py-16 space-y-10 px-8">
      <div className="text-center space-y-3 mb-12">
        <h1 className="text-5xl font-bold gradient-text">💬 커뮤니티</h1>
        <p className="text-xl text-gray-600">다양한 주제의 게시판에서 소통해보세요</p>
      </div>

      {!hasBoards ? (
        <div className="text-center py-20">
          <p className="text-gray-500 text-2xl">📭 등록된 게시판이 없습니다.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
          {data.map((b) => (
            <Link key={b.id} to={`/boards?categoryId=${b.id}`}>
              <Card className="h-full hover:-translate-y-1 hover:shadow-2xl transition-all duration-300 cursor-pointer group p-6">
                <div className="flex items-start gap-5 mb-5">
                  {b.imageUrl ? (
                    <img 
                      src={b.imageUrl} 
                      alt={b.name} 
                      className="w-20 h-20 rounded-xl object-cover shadow-md group-hover:shadow-lg transition-shadow" 
                    />
                  ) : (
                    <div className="w-20 h-20 rounded-xl bg-gradient-to-br from-purple-100 to-indigo-100 flex items-center justify-center shadow-md">
                      <span className="text-3xl">📋</span>
                    </div>
                  )}
                  <div className="flex-1">
                    <h2 className="text-xl font-bold text-gray-900 group-hover:text-purple-600 transition-colors">
                      {b.name}
                    </h2>
                    {b.description && (
                      <p className="text-base text-gray-600 mt-2 line-clamp-2">{b.description}</p>
                    )}
                  </div>
                </div>

                <div className="space-y-3 pt-5 border-t border-gray-100">
                  <div className="flex items-center justify-between text-base">
                    <span className="text-gray-600">📚 총 게시글</span>
                    <span className="font-semibold text-purple-600">{b.totalPosts}개</span>
                  </div>
                  <div className="flex items-center justify-between text-base">
                    <span className="text-gray-600">✨ 오늘 새 글</span>
                    <span className="font-semibold text-green-600">{b.todayPosts}개</span>
                  </div>
                  {b.latestTitle && (
                    <div className="mt-4 pt-4 border-t border-gray-100">
                      <p className="text-sm text-gray-500">최근 글</p>
                      <p className="text-base text-gray-700 truncate mt-1.5">{b.latestTitle}</p>
                    </div>
                  )}
                </div>
              </Card>
            </Link>
          ))}
        </div>
      )}
    </Container>
  );
}
