import { Link } from 'react-router-dom';
import type { Post } from '@/types/post';
import { Card, CardContent, CardHeader } from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import { extractAuthorRef, getDisplayName } from '@/utils/author';
import { formatDateYMDKorean } from '@/utils/date';

export function PostCard({ post, boardId }: { post: Post; boardId?: string | null }) {
  const { inlineName, memberId } = extractAuthorRef(post);
  const name = getDisplayName(memberId, inlineName);
  const link = boardId ? `/boards/${post.id}?categoryId=${boardId}` : `/boards/${post.id}`;

  return (
    <Card className="overflow-hidden">
      <CardHeader className="flex items-center gap-2">
        {!boardId && post.categoryName && <Badge>{post.categoryName}</Badge>}
        {post.isRecipe && <Badge className="bg-green-100 text-green-800 border-green-200">레시피</Badge>}
        <Link to={link} className="font-semibold hover:underline truncate">
          {post.title}
        </Link>
      </CardHeader>
      <CardContent className="text-sm text-gray-600">
        <div className="flex flex-wrap items-center gap-3">
          <span>{name}</span>
          <span>· {formatDateYMDKorean(post.createdAt)}</span>
          {post.updatedAt ? <span>· 수정 {formatDateYMDKorean(post.updatedAt)}</span> : null}
          <span>· 조회 {post.viewCount}</span>
          <span>· 추천 {post.likeCount}</span>
          <span>· 댓글 {post.commentCount}</span>
        </div>
      </CardContent>
    </Card>
  );
}

export default PostCard;
