// src/features/boards/pages/BoardDetailPage.tsx
import { useParams, Link, useNavigate } from 'react-router-dom';
import { usePost } from '@/features/boards/hooks/usePosts';
import { deletePost } from '@/apis/boards.api';
import CommentList from '@/features/comments/components/CommentList';
import { formatKST } from '@/utils/date';
import { extractAuthorRef } from '@/utils/author';
import { useMemberName } from '@/features/members/hooks/useMemberName';

export default function BoardDetailPage() {
  const { postId } = useParams();
  const id = Number(postId);
  const nav = useNavigate();

  // 훅은 항상 호출(조건부 호출 금지)
  const { data, isLoading, isError } = usePost(id);

  // 작성자 이름/식별자 안전 추출 → 이름 훅
  const { inlineName, memberId } = extractAuthorRef(data);
  const { name: authorName } = useMemberName(memberId, inlineName);

  // 렌더링 분기(훅 호출 순서는 유지)
  if (Number.isNaN(id)) return <div className="p-6">잘못된 글 ID입니다.</div>;
  if (isLoading) return <div className="p-6">불러오는 중…</div>;
  if (isError || !data) return <div className="p-6">글을 찾을 수 없거나 오류가 발생했습니다.</div>;

  const onDelete = async () => {
    if (!confirm('삭제하시겠습니까?')) return;
    try {
      await deletePost(id);
      nav('/boards');
    } catch (e: unknown) {
      const err = e as { response?: { status?: number; data?: { message?: string } } };
      alert(`삭제 실패: ${err.response?.status ?? ''} ${err.response?.data?.message ?? ''}`);
    }
  };

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-bold">{data.title}</h1>

      <div className="text-sm text-gray-600">
        {authorName ?? '익명'} · {formatKST(data.createdAt)}
        {data.updatedAt ? ` · 수정 ${formatKST(data.updatedAt)}` : null}
      </div>

      <div className="whitespace-pre-wrap">{data.content}</div>

      <div className="flex gap-3">
        <Link to={`/boards/${id}/edit`} className="underline">수정</Link>
        <button onClick={onDelete}>삭제</button>
      </div>

      {/* 댓글 */}
      <CommentList postId={id} />
    </div>
  );
}
