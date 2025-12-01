import { useEffect, useState } from 'react';
import { getMyScraps, toggleScrap, type ScrapItem } from '@/apis/scraps.api';
import type { Page } from '@/types/pagination';
import { useDebounce } from '@/hooks/useDebounce';
import { Card, CardContent, CardHeader, CardFooter } from '@/components/ui/Card';
import { useToast } from '@/contexts/ToastContext';

// Skeleton component for loading state
function ScrapSkeleton() {
  return (
    <Card className="animate-pulse">
      <div className="h-40 bg-gray-200" />
      <CardContent className="space-y-2">
        <div className="h-4 bg-gray-200 rounded w-3/4" />
        <div className="h-3 bg-gray-200 rounded w-1/2" />
        <div className="h-3 bg-gray-200 rounded w-full" />
      </CardContent>
      <CardFooter className="flex justify-end">
        <div className="h-6 bg-gray-200 rounded w-16" />
      </CardFooter>
    </Card>
  );
}

function ScrapCard({ item, onToggle }: { item: ScrapItem; onToggle: (postId: number) => void }) {
  const isRecipe = item.isRecipe;
  return (
    <Card className="flex flex-col overflow-hidden group">
      <a
        href={`/boards/${item.postId}`}
        className="block h-40 bg-gray-100 relative overflow-hidden"
        aria-label={`${item.title} 상세보기`}
      >
        {item.thumbnailUrl ? (
          <img
            src={item.thumbnailUrl}
            alt={item.title}
            className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
            loading="lazy"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-gray-400 text-xs">이미지 없음</div>
        )}
        <span className="absolute top-2 right-2 bg-black/50 text-white text-[10px] px-2 py-1 rounded">
          {isRecipe ? '레시피' : '게시글'}
        </span>
      </a>
      <CardHeader className="space-y-1">
        <h3 className="font-semibold text-base leading-snug line-clamp-2">{item.title}</h3>
        <p className="text-xs text-gray-500">by {item.authorName}</p>
      </CardHeader>
      <CardContent className="text-xs text-gray-600 flex flex-wrap gap-3">
        <span>❤️ {item.likeCount}</span>
        <span>👁️ {item.viewCount}</span>
        <span className="truncate">스크랩 {new Date(item.scrappedAt).toLocaleDateString()}</span>
      </CardContent>
      <CardFooter className="flex justify-between items-center">
        <button
          onClick={() => onToggle(item.postId)}
          className="text-xs px-3 py-1 rounded border hover:bg-gray-50 flex items-center gap-1"
          aria-label="스크랩 취소"
        >
          <span className="text-amber-500">★</span> 취소
        </button>
        <a href={`/boards/${item.postId}`} className="text-xs underline text-blue-600 hover:text-blue-700">
          상세보기
        </a>
      </CardFooter>
    </Card>
  );
}

export default function MyScrapsPage() {
  const [page, setPage] = useState(1);
  const [size] = useState(12);
  const [sortBy, setSortBy] = useState<'scrappedAt_desc' | 'scrappedAt_asc' | 'likes_desc'>('scrappedAt_desc');
  const [keyword, setKeyword] = useState('');
  const debouncedKeyword = useDebounce(keyword, 350);
  const { show } = useToast();

  const [data, setData] = useState<Page<ScrapItem> | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchList = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await getMyScraps({ page, size, sortBy, keyword: debouncedKeyword || undefined });
      setData(res);
    } catch {
      setError('목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchList();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, size, sortBy, debouncedKeyword]);

  const handleUnscrapOptimistic = async (postId: number) => {
    // optimistic removal
    setData((prev) => (prev ? { ...prev, content: prev.content.filter((c) => c.postId !== postId) } : prev));
    try {
      const { scrapped } = await toggleScrap(postId);
      show(scrapped ? '스크랩북에 추가했습니다.' : '스크랩북에서 삭제했습니다.', { type: 'success' });
      if (scrapped) {
        // We tried to cancel but server added; refetch to reconcile
        await fetchList();
      }
    } catch {
      show('스크랩 해제에 실패했습니다. 다시 시도해주세요.', { type: 'error' });
      await fetchList();
    }
  };

  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;

  return (
    <div className="max-w-7xl mx-auto p-6">
      <div className="flex items-end justify-between flex-wrap gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold">내 스크랩북</h1>
          <p className="text-sm text-gray-600 mt-1">총 {totalElements}개</p>
        </div>
        <div className="flex gap-2 items-center w-full sm:w-auto">
          <input
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="검색 (제목/내용)"
            className="border rounded px-3 py-2 text-sm flex-1 min-w-[160px]"
          />
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as any)}
            className="border rounded px-2 py-2 text-sm"
          >
            <option value="scrappedAt_desc">최근 스크랩순</option>
            <option value="scrappedAt_asc">오래된 스크랩순</option>
            <option value="likes_desc">인기순</option>
          </select>
        </div>
      </div>

      {error && <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 text-sm rounded">{error}</div>}

      {loading && (
        <div className="grid gap-4 grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
          {Array.from({ length: size }).map((_, i) => (
            <ScrapSkeleton key={i} />
          ))}
        </div>
      )}

      {!loading && data && data.content.length === 0 && (
        <div className="p-6 bg-gray-50 border rounded text-sm text-gray-700 space-y-3">
          <p>스크랩한 레시피가 없습니다. 관심 있는 레시피를 저장해보세요!</p>
          <a href="/boards" className="inline-block px-4 py-2 bg-blue-600 text-white rounded text-xs hover:bg-blue-700">
            레시피 목록으로 이동
          </a>
        </div>
      )}

      {!loading && data && data.content.length > 0 && (
        <div className="grid gap-4 grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
          {data.content.map((item) => (
            <ScrapCard key={item.scrapId} item={item} onToggle={handleUnscrapOptimistic} />
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="mt-8 flex justify-center items-center gap-3 text-sm">
          <button
            disabled={page <= 1}
            onClick={() => setPage((p) => p - 1)}
            className="px-3 py-1 border rounded disabled:opacity-40"
          >
            이전
          </button>
          <span>
            {page} / {totalPages}
          </span>
          <button
            disabled={page >= totalPages}
            onClick={() => setPage((p) => p + 1)}
            className="px-3 py-1 border rounded disabled:opacity-40"
          >
            다음
          </button>
        </div>
      )}
    </div>
  );
}
