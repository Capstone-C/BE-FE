import { useEffect, useState } from 'react';
import { getMyScraps, toggleScrap, type ScrapItem } from '@/apis/scraps.api';
import type { Page } from '@/types/pagination';
import { useDebounce } from '@/hooks/useDebounce';
import { useToast } from '@/contexts/ToastContext';
import { Link } from 'react-router-dom';
import { ThumbsUpIcon, EyeIcon, CalendarIcon, SearchIcon } from '@/components/ui/Icons';
import { formatYMDHMKorean } from '@/utils/date';

// Skeleton component for loading state
function ScrapSkeleton() {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden animate-pulse">
      <div className="h-48 bg-gray-200" />
      <div className="p-4 space-y-3">
        <div className="flex justify-between">
          <div className="h-4 bg-gray-200 rounded w-1/3" />
          <div className="h-4 bg-gray-200 rounded w-1/4" />
        </div>
        <div className="h-6 bg-gray-200 rounded w-3/4" />
        <div className="h-4 bg-gray-200 rounded w-1/2" />
        <div className="pt-4 flex justify-between items-center">
          <div className="h-8 bg-gray-200 rounded w-20" />
          <div className="h-8 bg-gray-200 rounded w-20" />
        </div>
      </div>
    </div>
  );
}

function ScrapCard({ item, onToggle }: { item: ScrapItem; onToggle: (postId: number) => void }) {
  const isRecipe = item.isRecipe;
  const dateStr = formatYMDHMKorean(item.postCreatedAt).split(' ')[0];

  return (
    <div className="group bg-white rounded-xl shadow-sm hover:shadow-md border border-gray-100 overflow-hidden transition-all duration-300 flex flex-col h-full">
      <Link to={`/boards/${item.postId}`} className="relative block overflow-hidden aspect-video bg-gray-100">
        {item.thumbnailUrl ? (
          <img
            src={item.thumbnailUrl}
            alt={item.title}
            className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
            loading="lazy"
          />
        ) : (
          <div className="w-full h-full flex flex-col items-center justify-center text-gray-400 bg-gray-50">
            <span className="text-4xl mb-2">🍳</span>
            <span className="text-xs">이미지 없음</span>
          </div>
        )}

        {/* Type Badge */}
        <div className="absolute top-3 left-3">
          <span className={`px-2.5 py-1 rounded-full text-[10px] font-bold text-white shadow-sm ${
            isRecipe ? 'bg-[#4E652F]' : 'bg-blue-500'
          }`}>
            {isRecipe ? '레시피' : '커뮤니티'}
          </span>
        </div>

        {/* Hover Overlay */}
        <div className="absolute inset-0 bg-black/0 group-hover:bg-black/5 transition-colors" />
      </Link>

      <div className="p-5 flex flex-col flex-1">
        <div className="flex justify-between items-start mb-2">
          <span className="text-xs text-gray-500 font-medium flex items-center gap-1 bg-gray-50 px-2 py-1 rounded">
             by {item.authorName}
          </span>
          <span className="text-[10px] text-gray-400 flex items-center gap-1" title={`스크랩 일시: ${formatYMDHMKorean(item.scrappedAt)}`}>
            <CalendarIcon className="w-3 h-3" /> {dateStr}
          </span>
        </div>

        <Link to={`/boards/${item.postId}`} className="block mb-3">
          <h3 className="text-lg font-bold text-gray-800 leading-snug line-clamp-2 group-hover:text-[#4E652F] transition-colors">
            {item.title}
          </h3>
        </Link>

        <div className="mt-auto pt-4 border-t border-gray-50 flex items-center justify-between">
          <div className="flex items-center gap-3 text-xs text-gray-500">
            <span className="flex items-center gap-1" title="좋아요">
              <ThumbsUpIcon className="w-3.5 h-3.5" /> {item.likeCount}
            </span>
            <span className="flex items-center gap-1" title="조회수">
              <EyeIcon className="w-3.5 h-3.5" /> {item.viewCount}
            </span>
          </div>

          <button
            onClick={(e) => {
              e.preventDefault();
              onToggle(item.postId);
            }}
            className="text-xs font-medium px-3 py-1.5 rounded-md border border-amber-200 text-amber-600 hover:bg-amber-50 transition-colors flex items-center gap-1.5"
          >
            <span className="text-amber-500 text-sm">★</span>
            스크랩 취소
          </button>
        </div>
      </div>
    </div>
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
    if (!confirm('스크랩북에서 삭제하시겠습니까?')) return;

    // optimistic removal
    setData((prev) => (prev ? { ...prev, content: prev.content.filter((c) => c.postId !== postId) } : prev));
    try {
      const { scrapped } = await toggleScrap(postId);
      show(scrapped ? '스크랩북에 추가했습니다.' : '스크랩북에서 삭제했습니다.', { type: 'success' });
      if (scrapped) {
        await fetchList(); // Re-fetch if state mismatch
      }
    } catch {
      show('스크랩 해제에 실패했습니다. 다시 시도해주세요.', { type: 'error' });
      await fetchList();
    }
  };

  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;

  return (
    <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-8 border-b border-gray-200 pb-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-2">
            내 스크랩북
            <span className="text-base font-normal text-gray-500 bg-gray-100 px-2 py-0.5 rounded-full">
              {totalElements}
            </span>
          </h1>
          <p className="mt-2 text-gray-500">
            저장한 레시피와 게시글을 모아보세요.
          </p>
        </div>

        <div className="flex flex-col sm:flex-row gap-3 w-full md:w-auto">
          <div className="relative flex-1 sm:flex-initial">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <SearchIcon className="h-4 w-4 text-gray-400" />
            </div>
            <input
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="제목, 내용 검색"
              className="block w-full sm:w-64 pl-10 pr-3 py-2 border border-gray-300 rounded-lg leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-[#4E652F] focus:border-[#4E652F] sm:text-sm transition duration-150 ease-in-out"
            />
          </div>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as any)}
            className="block w-full sm:w-auto pl-3 pr-8 py-2 text-base border-gray-300 focus:outline-none focus:ring-[#4E652F] focus:border-[#4E652F] sm:text-sm rounded-lg"
          >
            <option value="scrappedAt_desc">최근 스크랩순</option>
            <option value="scrappedAt_asc">오래된 스크랩순</option>
            <option value="likes_desc">인기순</option>
          </select>
        </div>
      </div>

      {error && (
        <div className="mb-8 p-4 bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg flex items-center justify-center">
          <span>⚠️ {error}</span>
          <button onClick={() => fetchList()} className="ml-3 underline font-medium hover:text-red-800">다시 시도</button>
        </div>
      )}

      {loading && (
        <div className="grid gap-6 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {Array.from({ length: size }).map((_, i) => (
            <ScrapSkeleton key={i} />
          ))}
        </div>
      )}

      {!loading && data && data.content.length === 0 && (
        <div className="flex flex-col items-center justify-center py-20 bg-gray-50 border-2 border-dashed border-gray-200 rounded-xl">
          <div className="text-6xl mb-4">📂</div>
          <h3 className="text-lg font-medium text-gray-900">스크랩한 글이 없습니다</h3>
          <p className="text-gray-500 mt-1 mb-6 text-sm">마음에 드는 레시피나 글을 저장하여 나만의 북마크를 만들어보세요!</p>
          <Link
            to="/boards"
            className="inline-flex items-center px-5 py-2.5 border border-transparent text-sm font-medium rounded-lg shadow-sm text-white bg-[#4E652F] hover:bg-[#425528] transition-colors"
          >
            게시판 둘러보기
          </Link>
        </div>
      )}

      {!loading && data && data.content.length > 0 && (
        <div className="grid gap-6 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {data.content.map((item) => (
            <ScrapCard key={item.scrapId} item={item} onToggle={handleUnscrapOptimistic} />
          ))}
        </div>
      )}

      {/* Pagination */}
      {!loading && totalPages > 1 && (
        <div className="mt-12 flex justify-center items-center gap-2">
          <button
            disabled={page <= 1}
            onClick={() => setPage((p) => p - 1)}
            className="px-4 py-2 border border-gray-300 rounded-md bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            이전
          </button>
          <div className="flex items-center gap-1 px-4">
            <span className="font-semibold text-[#4E652F]">{page}</span>
            <span className="text-gray-400">/</span>
            <span className="text-gray-600">{totalPages}</span>
          </div>
          <button
            disabled={page >= totalPages}
            onClick={() => setPage((p) => p + 1)}
            className="px-4 py-2 border border-gray-300 rounded-md bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            다음
          </button>
        </div>
      )}
    </div>
  );
}