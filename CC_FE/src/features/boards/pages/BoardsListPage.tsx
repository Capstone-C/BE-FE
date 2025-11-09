import { Link, useSearchParams, useLocation } from 'react-router-dom';
import { usePosts } from '@/features/boards/hooks/usePosts';
import { extractAuthorRef, getDisplayName } from '@/utils/author';
import { formatDateYMDKorean } from '@/utils/date';
import type { Post } from '@/types/post';

function PostListItem({ post, boardId }: { post: Post; boardId?: string | null }) {
  const { inlineName, memberId } = extractAuthorRef(post);
  const name = getDisplayName(memberId, inlineName);
  const listLink = boardId ? `/boards/${post.id}?categoryId=${boardId}` : `/boards/${post.id}`;

  return (
    <li className="border rounded-md p-3">
      <div className="flex items-center gap-2 text-sm text-gray-600">
        {!boardId && post.categoryName && (
          <span className="px-2 py-0.5 bg-gray-100 border rounded text-gray-700">{post.categoryName}</span>
        )}
        <span className="truncate flex-1">
          <Link to={listLink} className="font-medium hover:underline">
            {post.title}
          </Link>
        </span>
      </div>
      <div className="mt-1 text-xs text-gray-600 flex flex-wrap items-center gap-3">
        <span>{name}</span>
        <span>· {formatDateYMDKorean(post.createdAt)}</span>
        {post.updatedAt ? <span>· 수정 {formatDateYMDKorean(post.updatedAt)}</span> : null}
        <span>· 조회 {post.viewCount}</span>
        <span>· 추천 {post.likeCount}</span>
        <span>· 댓글 {post.commentCount}</span>
        {post.isRecipe ? <span className="ml-auto text-green-700">레시피</span> : null}
      </div>
    </li>
  );
}

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

  // 현재 보드에서 새 글을 작성할 경우, categoryId를 쿼리에 포함 + state로도 전달(보호 라우트 등으로 쿼리가 유실돼도 복구)
  const newPostHref = boardId ? `/boards/new?categoryId=${boardId}` : '/boards/new';
  const newPostState = boardId ? { fromCategoryId: Number(boardId) } : undefined;

  return (
    <div className="p-6 space-y-4">
      <div className="flex gap-3 items-center justify-between">
        <h1 className="text-xl font-semibold">{title}</h1>
        {!isMyPosts && (
          <Link to={newPostHref} state={newPostState} className="underline">
            새 글
          </Link>
        )}
      </div>

      <ul className="space-y-3">
        {data.content.map((p) => (
          <PostListItem key={p.id} post={p} boardId={boardId} />
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
