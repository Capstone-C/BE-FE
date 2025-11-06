import { Link, useSearchParams, useLocation } from 'react-router-dom';
import { usePosts } from '@/features/boards/hooks/usePosts';

export default function BoardsListPage() {
  const [sp, setSp] = useSearchParams();
  const location = useLocation();

  const page = Number(sp.get('page') ?? 1);
  const size = Number(sp.get('size') ?? 20);
  const boardId = sp.get('categoryId');
  const authorId = sp.get('authorId');
  const searchType = sp.get('searchType') ?? undefined;
  const keyword = sp.get('keyword') ?? undefined;
  const sortBy = sp.get('sortBy') ?? 'createdAt';

  const { data, isLoading, isError } = usePosts({
    page,
    size,
    keyword,
    sort: sortBy,
    boardId: boardId ? Number(boardId) : undefined,
    authorId: authorId ? Number(authorId) : undefined,
    searchType,
  });

  const isMyPosts = location.pathname.startsWith('/mypage/posts') || !!authorId;

  const title = isMyPosts ? '내가 작성한 글' : boardId ? '게시판 글 목록' : '전체 글';

  if (isLoading) return <div className="p-6">목록 불러오는 중…</div>;
  if (isError || !data) return <div className="p-6">오류가 발생했습니다.</div>;

  return (
    <div className="p-6 space-y-4">
      <div className="flex gap-3 items-center justify-between">
        <h1 className="text-xl font-semibold">{title}</h1>
        {!isMyPosts && (
          <Link to="/boards/new" className="underline">
            새 글
          </Link>
        )}
      </div>

      <ul className="space-y-2">
        {data.content.map((p) => (
          <li key={p.id}>
            <Link to={`/boards/${p.id}`} className="underline">
              {p.title}
            </Link>
          </li>
        ))}
      </ul>
      <div className="flex gap-2">
        <button disabled={page <= 1} onClick={() => setSp({ ...Object.fromEntries(sp), page: String(page - 1) })}>
          이전
        </button>
        <button
          disabled={page >= data.totalPages}
          onClick={() => setSp({ ...Object.fromEntries(sp), page: String(page + 1) })}
        >
          다음
        </button>
      </div>
    </div>
  );
}
