// src/features/boards/pages/BoardDetailPage.tsx
import { useParams, Link, useNavigate, useSearchParams } from 'react-router-dom';
import { usePost } from '@/features/boards/hooks/usePosts';
import { deletePost, toggleLike } from '@/apis/boards.api';
import CommentList from '@/features/comments/components/CommentList';
import { formatDateYMDKorean } from '@/utils/date';
import { extractAuthorRef, getDisplayName } from '@/utils/author';
import DOMPurify from 'dompurify';
import { useState } from 'react';

export default function BoardDetailPage() {
  const { postId } = useParams();
  const id = Number(postId);
  const nav = useNavigate();
  const [sp] = useSearchParams();

  const { data, isLoading, isError } = usePost(id);

  const { inlineName, memberId } = extractAuthorRef(data);
  const authorName = getDisplayName(memberId, inlineName);

  const [liked, setLiked] = useState<boolean | null>(null);
  const [likeCount, setLikeCount] = useState<number | null>(null);

  if (Number.isNaN(id)) return <div className="p-6">잘못된 글 ID입니다.</div>;
  if (isLoading) return <div className="p-6">불러오는 중…</div>;
  if (isError || !data) return <div className="p-6">글을 찾을 수 없거나 오류가 발생했습니다.</div>;

  const safeHtml = DOMPurify.sanitize(data.content ?? '');

  const onDelete = async () => {
    if (!confirm('삭제하시겠습니까?')) return;
    try {
      await deletePost(id);
      // 목록으로 돌아가기: boardId가 있으면 유지
      const to = sp.get('categoryId') ? `/boards?categoryId=${sp.get('categoryId')}` : '/boards';
      nav(to);
      alert('게시글이 성공적으로 삭제되었습니다.');
    } catch (e: unknown) {
      const err = e as { response?: { status?: number; data?: { message?: string } } };
      alert(`삭제 실패: ${err.response?.status ?? ''} ${err.response?.data?.message ?? ''}`);
    }
  };

  const onToggleLike = async () => {
    try {
      const res = await toggleLike(id);
      setLiked(res.liked);
      setLikeCount(res.likeCount);
    } catch (err) {
      alert('로그인이 필요한 기능입니다.');
      nav('/login');
    }
  };

  const currentLiked = liked ?? (data as unknown as { likedByMe?: boolean }).likedByMe ?? false;
  const currentLikeCount = likeCount ?? data.likeCount ?? 0;

  const editHref = sp.get('categoryId')
    ? `/boards/${id}/edit?categoryId=${sp.get('categoryId')}`
    : `/boards/${id}/edit`;
  const editState = sp.get('categoryId') ? { fromCategoryId: Number(sp.get('categoryId')) } : undefined;

  return (
    <div className="p-6 space-y-4">
      <div className="text-sm text-blue-600">
        <Link
          to={sp.get('categoryId') ? `/boards?categoryId=${sp.get('categoryId')}` : '/boards'}
          className="hover:underline"
        >
          {data.categoryName ?? '게시판'} 목록
        </Link>
      </div>

      <h1 className="text-2xl font-bold">{data.title}</h1>

      <div className="text-sm text-gray-600 flex flex-wrap gap-2">
        <span>{authorName ?? '익명'}</span>
        <span>· {formatDateYMDKorean(data.createdAt)}</span>
        {data.updatedAt ? <span>· 수정 {formatDateYMDKorean(data.updatedAt)}</span> : null}
        <span>· 조회 {data.viewCount}</span>
        <span>· 추천 {currentLikeCount}</span>
      </div>

      <div className="prose max-w-none" dangerouslySetInnerHTML={{ __html: safeHtml }} />

      <div className="flex gap-3 items-center">
        <button
          onClick={onToggleLike}
          className={`border px-3 py-1 rounded ${currentLiked ? 'bg-blue-600 text-white' : ''}`}
        >
          {currentLiked ? '추천 취소' : '추천'} ({currentLikeCount})
        </button>
        <Link to={editHref} state={editState} className="underline">
          수정
        </Link>
        <button onClick={onDelete}>삭제</button>
      </div>

      <hr />

      <CommentList postId={id} />
    </div>
  );
}
