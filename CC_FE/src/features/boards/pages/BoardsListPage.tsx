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
    <Container className="py-16 space-y-10 px-8 max-w-7xl">
      <div className="flex gap-4 items-center justify-between">
        <h1 className="text-5xl font-bold gradient-text">📝 {title}</h1>
        {!isMyPosts && (
          <Link to={newPostHref} state={newPostState}>
            <Button size="lg">✨ 새 글 작성</Button>
          </Link>
        )}
      </div>

      <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
        {visiblePosts.map((p) => (
          <PostCard key={p.id} post={p} boardId={boardId} />
        ))}
      </div>

      <div className="flex gap-4 justify-center pt-10">
        <Button
          variant="secondary"
          size="lg"
          disabled={page <= 1}
          onClick={() => setSp({ ...Object.fromEntries(sp), page: String(page - 1) })}
        >
          이전
        </Button>
        <Button
          variant="secondary"
          size="lg"
          disabled={page >= data.totalPages}
          onClick={() => setSp({ ...Object.fromEntries(sp), page: String(page + 1) })}
        >
          다음
        </Button>
      </div>
    </div>
  );
}