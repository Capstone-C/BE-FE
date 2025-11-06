import { Link, useSearchParams } from 'react-router-dom';
import { usePosts } from '@/features/boards/hooks/usePosts';

export default function BoardsListPage() {
  const [sp, setSp] = useSearchParams();
  const page = Number(sp.get('page') ?? 0);
  const { data, isLoading, isError } = usePosts({ page, size: 10, keyword: sp.get('keyword') ?? '' });

  if (isLoading) return <div className="p-6">목록 불러오는 중…</div>;
  if (isError || !data) return <div className="p-6">오류가 발생했습니다.</div>;

  return (
    <div className="p-6 space-y-4">
      <div className="flex gap-3 items-center">
        <Link to="/boards/new" className="underline">새 글</Link>
      </div>
      <ul className="space-y-2">
        {data.content.map((p) => (
          <li key={p.id}>
            <Link to={`/boards/${p.id}`} className="underline">{p.title}</Link>
          </li>
        ))}
      </ul>
      <div className="flex gap-2">
        <button disabled={page<=0} onClick={()=> setSp({ page: String(page-1) })}>이전</button>
        <button disabled={page>=data.totalPages-1} onClick={()=> setSp({ page: String(page+1) })}>다음</button>
      </div>
    </div>
  );
}
