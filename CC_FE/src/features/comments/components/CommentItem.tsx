// CommentItem.tsx
import { useState, memo } from 'react';
import { Link } from 'react-router-dom';
import type { Comment } from '@/types/comment';
import CommentForm from './CommentForm';
import { useDeleteComment } from '../hooks/useCommentMutations';
import { extractAuthorRef, getDisplayName } from '@/utils/author';
import { formatDateYMDKorean } from '@/utils/date';
import {
  useBlockMemberMutation,
  useUnblockMemberMutation,
  useBlockedMembers,
} from '@/features/members/hooks/useMemberBlocks';
import { useToast } from '@/contexts/ToastContext';
import type { BlockedMember } from '@/apis/memberBlocks';

type Props = { c: Comment; postId: number };

function CommentItemImpl({ c, postId }: Props) {
  const [replyOpen, setReplyOpen] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const del = useDeleteComment(postId, c.id);
  const { show } = useToast();
  const blockMutation = useBlockMemberMutation();
  const unblockMutation = useUnblockMemberMutation();
  const { data: blockedList } = useBlockedMembers();

  // 서버가 내려준 이름(있으면 우선), 없으면 memberId로 로컬 폴백
  const { inlineName, memberId } = extractAuthorRef(c);
  const authorName = getDisplayName(memberId, inlineName);
  const authorNode = memberId ? (
    <Link to={`/members/${memberId}`} className="hover:underline">
      {authorName}
    </Link>
  ) : (
    <span>{authorName}</span>
  );

  const isAuthorBlocked = memberId != null && blockedList?.some((b: BlockedMember) => b.blockedId === memberId);
  const onToggleBlock = async () => {
    if (!memberId) return;
    try {
      if (isAuthorBlocked) {
        if (!confirm('이 회원 차단을 해제하시겠습니까?')) return;
        await unblockMutation.mutateAsync(memberId);
        show('차단이 해제되었습니다.', { type: 'success' });
      } else {
        if (!confirm('이 회원을 차단하시겠습니까?')) return;
        await blockMutation.mutateAsync(memberId);
        show('해당 회원이 차단되었습니다.', { type: 'success' });
      }
    } catch (e: any) {
      const message = e?.response?.data?.message as string | undefined;
      show(message ?? '차단 처리 중 오류가 발생했습니다.', { type: 'error' });
    }
  };

  return (
    <li className="border-b py-3">
      <div className="text-sm text-gray-600">
        {authorNode} · {formatDateYMDKorean(c.createdAt)}
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
        {memberId && (
          <button
            onClick={onToggleBlock}
            disabled={blockMutation.isPending || unblockMutation.isPending}
            className={isAuthorBlocked ? 'text-red-600' : undefined}
          >
            {isAuthorBlocked ? '차단 해제' : '차단'}
          </button>
        )}
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
