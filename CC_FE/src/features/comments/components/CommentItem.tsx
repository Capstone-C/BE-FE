// CommentItem.tsx
import { useState, memo } from 'react';
import type { Comment } from '@/types/comment';
import CommentForm from './CommentForm';
import { useDeleteComment } from '../hooks/useCommentMutations';
import { extractAuthorRef, getDisplayName } from '@/utils/author';
import { formatKST } from '@/utils/date';

type Props = { c: Comment; postId: number };

function CommentItemImpl({ c, postId }: Props) {
  const [replyOpen, setReplyOpen] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const del = useDeleteComment(postId, c.id);

  // 서버가 내려준 이름(있으면 우선), 없으면 memberId로 로컬 폴백
  const { inlineName, memberId } = extractAuthorRef(c);
  const authorName = getDisplayName(memberId, inlineName);

  return (
    <li className="border-b py-3">
      <div className="text-sm text-gray-600">
        {authorName} · {formatKST(c.createdAt)}
      </div>

      {!editOpen ? (
        <div className="whitespace-pre-wrap">{c.content}</div>
      ) : (
        <CommentForm mode="edit" postId={postId} commentId={c.id} initial={c.content} />
      )}

      <div className="flex gap-2 mt-2 text-sm">
        <button onClick={() => setReplyOpen((v) => !v)}>답글</button>
        <button onClick={() => setEditOpen((v) => !v)}>{editOpen ? '취소' : '수정'}</button>
        <button
          onClick={async () => {
            if (confirm('삭제하시겠습니까?')) await del.mutateAsync();
          }}
        >
          삭제
        </button>
      </div>

      {replyOpen && (
        <div className="ml-4 mt-2">
          <CommentForm mode="create" postId={postId} parentId={c.id} />
        </div>
      )}

      {/* 자식 댓글 트리 */}
      {c.children?.length ? (
        <ul className="ml-4 mt-3 space-y-2">
          {c.children.map((cc) => (
            <MemoCommentItem key={cc.id} c={cc} postId={postId} />
          ))}
        </ul>
      ) : null}
    </li>
  );
}

/** 동일 댓글 재렌더 최소화 (옵션) */
const MemoCommentItem = memo(CommentItemImpl);
export default MemoCommentItem;
