import { Link, useSearchParams, useLocation } from 'react-router-dom';
import { useEffect, useMemo, useState } from 'react';
import { usePosts } from '@/features/boards/hooks/usePosts';
import Container from '@/components/ui/Container';
import Button from '@/components/ui/Button';
import { PostCard } from '@/features/boards/components/PostCard';
import { listCategories, type Category } from '@/apis/categories.api';
import { useBlockedMembers } from '@/features/members/hooks/useMemberBlocks';
import { extractAuthorRef } from '@/utils/author';

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

  const { data: blocked } = useBlockedMembers();

  const isMyPosts = location.pathname.startsWith('/mypage/posts') || !!authorId;

  const title = isMyPosts ? '내가 작성한 글' : boardId ? '게시판 글 목록' : '전체 글';

  const [categories, setCategories] = useState<Category[] | null>(null);

  useEffect(() => {
    let alive = true;
    (async () => {
      try {
        const list = await listCategories();
        if (!alive) return;
        setCategories(list);
      } catch {
        // ignore
      }
    })();
    return () => {
      alive = false;
    };
  }, []);

  const isRecipeCategory = useMemo(() => {
    if (!boardId) return false;
    if (!categories) return false;
    const cid = Number(boardId);
    const found = categories.find((c) => c.id === cid);
    return found?.type === 'RECIPE';
  }, [boardId, categories]);

  if (isLoading) return <div className="p-6">목록 불러오는 중…</div>;
  if (isError || !data) return <div className="p-6">오류가 발생했습니다.</div>;

  const newPostHref = isRecipeCategory ? '/recipes/new' : '/boards/new';
  const newPostState = !isRecipeCategory && boardId ? { fromCategoryId: Number(boardId) } : undefined;

  const blockedIds = blocked?.map((b) => b.blockedId) ?? [];
  const visiblePosts = data.content.filter((p) => {
    const { memberId } = extractAuthorRef(p as any);
    return !(memberId && blockedIds.includes(memberId));
  });

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
    </Container>
  );
}
