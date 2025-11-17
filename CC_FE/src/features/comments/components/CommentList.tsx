import { useComments } from '../hooks/useComments';
import CommentItem from './CommentItem';
import CommentForm from './CommentForm';
import type { Comment } from '@/types/comment';
import { useBlockedMembers } from '@/features/members/hooks/useMemberBlocks';
import { extractAuthorRef } from '@/utils/author';

// 서버가 평면 배열을 반환하고 parentId로 계층을 표현하는 경우, FE에서 트리로 변환
function toTree(list: Comment[]) {
  const map = new Map<number, Comment & { children: Comment[] }>();
  const roots: (Comment & { children: Comment[] })[] = [];
  list.forEach((c) => map.set(c.id, { ...c, children: c.children ?? [] }));
  list.forEach((c) => {
    const node = map.get(c.id)!;
    if (c.parentId) {
      map.get(c.parentId)?.children.push(node);
    } else {
      roots.push(node);
    }
  });
  return roots;
}

export default function CommentList({ postId }: { postId: number }) {
  const { data, isLoading, isError } = useComments(postId);
  const { data: blocked } = useBlockedMembers();
  if (isLoading) return <div>댓글 불러오는 중…</div>;
  if (isError || !data) return <div>댓글을 불러오지 못했습니다.</div>;

  const blockedIds = blocked?.map((b) => b.blockedId) ?? [];
  const tree = toTree(data);
  const prune = (nodes: any[]): any[] =>
    nodes
      .filter((n) => {
        const { memberId } = extractAuthorRef(n);
        return !(memberId && blockedIds.includes(memberId));
      })
      .map((n) => ({ ...n, children: prune(n.children) }));
  const filteredTree = prune(tree);

  return (
    <div className="mt-6 space-y-4">
      <h2 className="font-semibold">댓글 ({data.length})</h2>
      <CommentForm mode="create" postId={postId} />
      <ul className="space-y-2">
        {filteredTree.map((c) => (
          <CommentItem key={c.id} c={c} postId={postId} />
        ))}
      </ul>
    </div>
  );
}
